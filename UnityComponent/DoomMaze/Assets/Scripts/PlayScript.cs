/*
Authors: Martin Edmunds
Project: Project Doom
Date: 07/07/2020
Version: 1.0
*/﻿

using System.Globalization;
using UnityEngine;
using UnityEngine.UI;

/*
Class that controls the menu screen logic
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

    /*
    Swaps currently enabled camera to the maze camera
    */
    void ToggleCamera()
    {
        mazeCamera.enabled = !mazeCamera.enabled;
        mainCamera.enabled = !mainCamera.enabled;
    }

    void DisableButtons()
    {
        playButton.gameObject.SetActive(false);
        exitButton.gameObject.SetActive(false);
    }

    void Start()
    {
        //disable start button and slider
        startButton.gameObject.SetActive(false);
        sizeSlider.gameObject.SetActive(false);

        playButton.onClick.AddListener(TaskOnClick);
        mainCamera.enabled = true;
        mazeCamera.enabled = false;
    }

    void TaskOnClick()
    {
        UnityEngine.Debug.Log("Starting Game...");

        //swap cameras
        ToggleCamera();
        DisableButtons();

        //enable start buttons
        startButton.gameObject.SetActive(true);
        sizeSlider.gameObject.SetActive(true);
        script.SetRunFlag(true);

    }
}
