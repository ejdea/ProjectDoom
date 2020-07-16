/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using UnityEditor.PackageManager;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.UIElements;

/*
 * Class that controls the authentication flow for the user.
 * Assumes that the user has created an account with the DoomMaze camera app
 */
public class AuthScript : MonoBehaviour
{
    public GameObject emailField;
    public GameObject passField;
    public GameObject sceneLoader;
    public GameObject errorText;

    // Holds the current user, need to download file from database
    public Firebase.Auth.FirebaseUser curUser = null;

    // Start is called before the first frame update
    void Start()
    {
    }

    public async void StartAuthFlowAsync()
    {
        Debug.Log("Starting AuthFlow");

        Firebase.Auth.FirebaseAuth auth = Firebase.Auth.FirebaseAuth.DefaultInstance;
        string email = emailField.GetComponent<InputField>().text;
        string password = passField.GetComponent <InputField>().text;
        bool auth_result = false;
        if(!ValidateEmailPass(email, password))
        {
            errorText.SetActive(true);
            return;
        }

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

        if(curUser != null && auth_result)
        {
            // load next scene
            errorText.SetActive(false);
            sceneLoader.GetComponent<SceneLoader>().LoadNextScene();
        }
        else
        {
            // problem authorizing the user
            errorText.SetActive(true);
        }

    }

    //Pull heightmap from google database and call ModifyTerrain 
    public async void LoadCurrentHeightMap()
    {
    }

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
