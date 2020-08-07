/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/17/2020
 * Version: 1.0
 */

using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.UI;
using System.Security.Cryptography;
using UnityEngine.SceneManagement;
using UnityEngine.UIElements;
using Firebase;
using Firebase.Firestore;
using System.IO;
using Firebase.Database;
using Firebase.Unity.Editor;
using System;
using Firebase.Extensions;


/*
 * Class that controls the authentication flow for the user and downloads a heightmap if applicable
 * Assumes that the user has created an account with the DoomMaze camera app
 */
public class AuthScript : MonoBehaviour
{
    public GameObject emailField;
    public GameObject passField;
    public GameObject sceneLoader;
    public GameObject errorText;

    // Holds the current user, need to download file from database
    public static Firebase.Auth.FirebaseUser curUser = null;
    public static double mapHighScore = -1.0;
    public static string map_hash_value = "";
    public Firebase.Storage.StorageReference storage_ref = null;


    // Firebase variables
    public const string file_name = "mobile_height_map.raw";
    public const string app_folder = "app_height_maps";

    // Data that will be read by ModifyTerrain upon scene load
    public static byte[] heightData = null;

    // Start is called before the first frame update
    void Start()
    {
        Firebase.Storage.FirebaseStorage storage = Firebase.Storage.FirebaseStorage.DefaultInstance;

        // Get the root reference location of the database.
        storage_ref = storage.GetReferenceFromUrl("gs://doodle-maze-2020.appspot.com");

        // Ensures members of this class can be accessed by other scripts in different scenes
        DontDestroyOnLoad(this.gameObject);

        // Disable screen dimming
        Screen.sleepTimeout = SleepTimeout.NeverSleep;
    }

    /*
     * Function that performs the following:
     *  1. Pulls input from the unity form
     *  2. Validates the input
     *  3. Attempts to authenticate the user with username/password using FirebaseAuth
     *  4. If the user is authenticated, a map is checked for at the location {app_url}/app_height_maps/{user_id}/mobile_height_map.raw"
     *      a. if the map is found, continues with game loop. Terrain will be build on next scene load.
     *      b. if map is not found, populates text box with error message
     * 
     * TODO: For final product, the .raw height map might have an additional 20 bytes of position data that will need to be read to place the game objects.
     * 
     */
    public async void StartAuthFlowAsync()
    {
        Debug.Log("Starting AuthFlow");

        // Get auth instance
        Firebase.Auth.FirebaseAuth auth = Firebase.Auth.FirebaseAuth.DefaultInstance;
        string email = emailField.GetComponent<InputField>().text;
        string password = passField.GetComponent <InputField>().text;
        bool auth_result = false;
        // Validate input
        if(!ValidateEmailPass(email, password))
        {
            SetErrorTextBox(true, "Invalid Username/Password");
            return;
        }

        //attempt to sign the user in, wait to proceed
        await auth.SignInWithEmailAndPasswordAsync(email, password).ContinueWith(task =>
        {
            if (task.IsCanceled)
            {
                Debug.LogError("SignInWithCustomTokenAsync was canceled.");
                return;
            }
            if (task.IsFaulted)
            {
                Debug.LogError("SignInWithCustomTokenAsync encountered an error: " + task.Exception);
                return;
            }
            curUser = task.Result;
            auth_result = true;
        });

        //if the user was able to log in, continue with file download attempt
        if(curUser != null && auth_result)
        {
            const long maxSize = 3 * 1024 * 1024; //3mb limit (1025x1025x16bit + 32 bytes position data map should always be ~ 2MB)
            string path = app_folder + "/" + curUser.UserId + "/" + file_name; ; //{app_url}/app_height_maps/{userID}/mobile_height_map.raw

            // Get storage instance
            Firebase.Storage.FirebaseStorage storage = Firebase.Storage.FirebaseStorage.DefaultInstance;
            string url = storage.RootReference + path;
            Firebase.Storage.StorageReference gs_reference = storage.GetReferenceFromUrl(url);

            // Download the .raw heightmap file
            await gs_reference.GetBytesAsync(maxSize).ContinueWith((Task<byte[]> task) =>
            {
                if (task.IsFaulted || task.IsCanceled)
                {
                    Debug.Log(task.Exception.ToString());
                    heightData = null;
                }
                else
                {
                    heightData = task.Result;
                    CheckForHighScore();
                }
            });

            if(heightData != null)
            {
                //something was able to be downloaded
                SetErrorTextBox(false);
                sceneLoader.GetComponent<SceneLoader>().LoadNextScene();
            }
            else
            {
                //file was unable to be downloaded
                SetErrorTextBox(true, "No heightmap available!");
            }
        }
        else
        {
            // problem authorizing the user
            SetErrorTextBox(true, "Authorization Failed!");
        }
    }

    /**
     * Signs the user out of Firebase and returns to the login screen
     * 
     */
    public void signOut()
	{
        Debug.Log("Signing out");

        Firebase.Auth.FirebaseAuth auth = Firebase.Auth.FirebaseAuth.DefaultInstance;
        auth.SignOut();

        int prevSceneIndex = SceneManager.GetActiveScene().buildIndex - 1;

        if (prevSceneIndex < 0)
            prevSceneIndex = 0;

        SceneManager.LoadScene(prevSceneIndex);
    }

    /**
     * Function that
     *  1. Hashes the downloaded map data
     *  2. Checks to see if the map has a current high score
     * 
     * 
     */
    public void CheckForHighScore()
    {

        FirebaseFirestore db = FirebaseFirestore.DefaultInstance;
        CollectionReference colRef = db.Collection("Maps");

        // Get hash value for map data
        SHA256Managed hash = new SHA256Managed();
        byte[] result = hash.ComputeHash(heightData);
        string hashValue = "";
        foreach (byte b in result)
        {
            hashValue += b.ToString("x2");
        }
        map_hash_value = hashValue;

        //query the db to see if the map exists
        Firebase.Firestore.Query query = colRef.WhereEqualTo("hash", hashValue);
        query.GetSnapshotAsync().ContinueWithOnMainThread((querySnapshotTask) =>
        {
            foreach (DocumentSnapshot docSnap in querySnapshotTask.Result.Documents)
            {
                Dictionary<string, object> mapObj = docSnap.ToDictionary();
                var mapScore = mapObj["score"];
                double mapScoreD;
                double.TryParse(mapScore.ToString(), out mapScoreD);
                mapHighScore = mapScoreD;
            }
        });

    }


    /*
     * Debug function to ensure firebase storage is working correctly
     * 
     */
    public async void writeMap(byte[] data)
    {

        string path = app_folder + "/" + curUser.UserId + "/" + file_name;

        // Create a reference to 'app_height_maps/{userID}/"mobile_height_map.raw"
        Firebase.Storage.StorageReference abs_path =
          storage_ref.Child(path);

        await abs_path.PutBytesAsync(data).ContinueWith((Task<Firebase.Storage.StorageMetadata> task) =>
        {
            if (task.IsFaulted || task.IsCanceled)
            {
                Debug.Log(task.Exception.ToString());
                // Uh-oh, an error occurred!
            }
            else
            {
                // Metadata contains file metadata such as size, content-type, and download URL.
                Firebase.Storage.StorageMetadata metadata = task.Result;
                Debug.Log("Finished uploading...");
            }
        });

    }

    /**
     * Wrapper for setting text box error messages on the Unity Auth Form
     * 
     */
    private void SetErrorTextBox(bool visible, string message = "")
    {
        if (visible == false)
        {
            errorText.SetActive(false);
            return;
        }
        else
        {
            errorText.GetComponent<Text>().text = message;
            errorText.SetActive(true);
            return;
        }
    }

    /*
     * Validates input entered in the InputFields in the AuthScene, for now just checks for simple nulls
     *
     */
    private bool ValidateEmailPass(string email, string password)
    {
        if(email == null || password == null || email == "" || password == "")
        {
            return false;
        }
        return true;
    }

    // Update is called once per frame
    void Update()
    {
        
    }



}
