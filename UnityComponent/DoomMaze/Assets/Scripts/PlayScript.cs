/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

using System.Globalization;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;

/*
 * Class that controls the menu screen logic
 */
public class PlayScript : MonoBehaviour
{
    public Button playButton;
    public Button exitButton;
    public Button startButton;
    public Slider sizeSlider;

    public Camera mainCamera;
    public Camera mazeCamera;

    public StartScript script;

    // Swaps currently enabled camera to the maze camera
    void ToggleCamera()
    {
        mazeCamera.enabled = !mazeCamera.enabled;
        mainCamera.enabled = !mainCamera.enabled;
    }

    void Start()
    {
        //disable start button and slider
        startButton.gameObject.SetActive(false);
        sizeSlider.gameObject.SetActive(false);

        mainCamera.enabled = true;
        mazeCamera.enabled = false;

        // Start game
        UnityEngine.Debug.Log("Starting Game...");

        //swap cameras
        ToggleCamera();

        //enable start buttons
        startButton.gameObject.SetActive(true);
        sizeSlider.gameObject.SetActive(true);
        script.SetRunFlag(true);
    }
}
