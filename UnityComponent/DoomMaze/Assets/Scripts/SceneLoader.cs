/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class SceneLoader : MonoBehaviour
{
    private const int startSceneIdx = 0;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void LoadNextScene()
    {
        int nextSceneIndex = SceneManager.GetActiveScene().buildIndex + 1;

        if (nextSceneIndex >= SceneManager.sceneCountInBuildSettings)
            nextSceneIndex = SceneManager.sceneCountInBuildSettings - 1;

        SceneManager.LoadScene(nextSceneIndex);
    }

    public void LoadPrevScene()
    {
        int prevSceneIndex = SceneManager.GetActiveScene().buildIndex - 1;

        if (prevSceneIndex < 0)
            prevSceneIndex = 0;

        SceneManager.LoadScene(prevSceneIndex);
    }

    public void StartScene()
    {
        SceneManager.LoadScene(startSceneIdx);
    }

    public void QuitGame()
    {
#if UNITY_EDITOR
        UnityEditor.EditorApplication.isPlaying = false;
#else
        Application.Quit();
#endif
    }
}
