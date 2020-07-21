/*
Authors: Martin Edmunds, Edmund Dea, Lee Rice
Project: Project Doom
Date: 07/07/2020
Version: 1.0
*/

using UnityEngine;
using UnityEngine.SceneManagement;


// Script that handles game logic whenever the player reaches the end of the maze
public class EndScript : MonoBehaviour
{
    private bool isEnabled = false;

    // Start is called before the first frame update
    void Start()
    {
        // Disable screen dimming
        Screen.sleepTimeout = SleepTimeout.NeverSleep;
    }

    // Update is called once per frame
    void Update()
    {
        
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
            int currentSceneIndex = SceneManager.GetActiveScene().buildIndex;
            SceneManager.LoadScene(currentSceneIndex + 1);
        }
    }

}
