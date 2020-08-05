/*
Authors: Martin Edmunds, Edmund Dea, Lee Rice
Project: Project Doom
Date: 07/07/2020
Version: 1.0
*/

using System.ComponentModel;
using System.Security.Cryptography;
using Firebase.Firestore;
using Firebase.Extensions;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.Video;
using System.Collections.Generic;
using System.Diagnostics.Tracing;


// Script that handles game logic whenever the player reaches the end of the maze
public class EndScript : MonoBehaviour
{
    private bool isEnabled = false;
    public GameObject scoreDisplay;
    public VideoPlayer videoPlayer;
    public AudioSource audioSource;
    GameObject uiContainer;
    private bool initialized = false;

    // Start is called before the first frame update
    void Start()
    {
        /* The preparation consists of reserving the resources needed for
         * playback, and preloading some or all of the content to be played.
         * After this is done, frames can be received immediately and all
         * properties related to the source can be queried.
         */
        GameObject video = GameObject.Find("EndVideoPlayer");
        if (video)
		{
            videoPlayer = video.GetComponent<VideoPlayer>();
            videoPlayer.Prepare();
            audioSource = videoPlayer.GetComponent<AudioSource>();
        }        

        // Get UIContainer
        uiContainer = GameObject.Find("EndUIContainer");
        if (uiContainer)
		{
            uiContainer.SetActive(false);
        }

        // Disable screen dimming
        Screen.sleepTimeout = SleepTimeout.NeverSleep;
    }

    // Update is called once per frame
    void Update()
    {
        if (!initialized && videoPlayer && videoPlayer.isPlaying)
		{
            audioSource.Play();
            uiContainer.SetActive(true);
            initialized = true;
        }
    }

    // Set whether the end box is enabled
    public void enableEndBox(bool status)
    {
        isEnabled = status;
    }

    /* Unity triggers this on load. I've wrapped the event with a flag
     * that can be set to 'true' after the game officially starts.
     */
    private void OnTriggerEnter(Collider other)
    {
        if (isEnabled)
        {
            //if a map hasn't been played, or if there is a new high score, log the new score
            if (AuthScript.mapHighScore < 0.0 || GetCurrentScore() < AuthScript.mapHighScore)
            {
                LogScore(GetCurrentScore());
            }
            int currentSceneIndex = SceneManager.GetActiveScene().buildIndex;
            SceneManager.LoadScene(currentSceneIndex + 1);
        }
    }

    private double GetCurrentScore()
    {
        ScoreScript script = scoreDisplay.GetComponent<ScoreScript>();
        return script.GetTime();
    }

    /**
     * Function that checks if a new high score has been achieved for the current map
     * 
     * If there's a new high score, the document is updated with the new score
     * 
     */
    private void LogScore(double score)
    {

        FirebaseFirestore db = FirebaseFirestore.DefaultInstance;
        CollectionReference colRef = db.Collection("Maps");

        //query the db to see if the map exists
        Query query = colRef.WhereEqualTo("hash", AuthScript.map_hash_value);
        query.GetSnapshotAsync().ContinueWithOnMainThread((querySnapshotTask) =>
        {
            foreach (DocumentSnapshot docSnap in querySnapshotTask.Result.Documents)
            {
                //if new high score
                Dictionary<string, object> mapObj = docSnap.ToDictionary();
                
                //update local and cloud high score
                AuthScript.mapHighScore = score;
                mapObj["score"] = score;
                DocumentReference mapRef = db.Collection("Maps").Document(docSnap.Id);
                mapRef.UpdateAsync(mapObj).ContinueWithOnMainThread(task =>
                { });
            }
        });
    }
}
