using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Video;

public class StartSceneScript : MonoBehaviour
{
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
        GameObject video = GameObject.Find("StartVideoPlayer");
        if (video)
        {
            videoPlayer = video.GetComponent<VideoPlayer>();
            videoPlayer.Prepare();
            audioSource = videoPlayer.GetComponent<AudioSource>();
        }

        // Get UIContainer
        uiContainer = GameObject.Find("StartUIContainer");
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
        if (!initialized && videoPlayer.isPlaying)
        {
            audioSource.Play();
            uiContainer.SetActive(true);
            initialized = true;
        }
    }
}
