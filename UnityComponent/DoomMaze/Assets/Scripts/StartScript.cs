/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

using Firebase.Extensions;
using Firebase.Firestore;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Numerics;
using System.Security.Cryptography;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

/*
 * Script to control the start up sequence of the game 
 * IN-DEVELOPMENT
 */
public class StartScript : MonoBehaviour
{
    public Button startButton;
    public Button restartButton;
    public Button quitButton;
    public Slider sizeSlider;
    public GameObject score;
    public GameObject highScore;
    public GameObject playerSphere;
    public GameObject mazeTerrain;
    public GameObject endBox;
    public Camera currentCamera;

    public GameObject selected;
    public GameObject prevSelected;

    private float scale_factor = 5;

    private bool gameStarted = false;
    private bool script_start_flag = false;
    private bool gameObjectPositionsSet = false;
    private const int TerrainResolution = 1025;
    private const float min_scale_value = 1.5f;
    private const float max_scale_value = 5.0f;
    private const float PlayerObjectScaleFactor = 1.0f;
    private const float ObjectScaleFactor = 0.5f;
    Color prev_color;

    // Start is called before the first frame update
    void Start()
    {
        //Clamp player sphere
        UnityEngine.Vector3 curScale = playerSphere.transform.localScale;

        // Add listener to startButton
        startButton.onClick.AddListener(TaskOnClick);
        Text text_c = score.GetComponent<Text>();
        text_c.enabled = false;
        Text htext_c = highScore.GetComponent<Text>();
        htext_c.enabled = false;

        // Add listener to mid-game buttons
        restartButton.onClick.AddListener(RestartOnClick);
        quitButton.onClick.AddListener(QuitOnClick);

        // Start slider with half value
        sizeSlider.value = 0.5f;
        OnSliderChange(sizeSlider);

        // Bind slider value change method
        sizeSlider.onValueChanged.AddListener(delegate
        {
            OnSliderChange(sizeSlider);
        });

        // Disable screen dimming
        Screen.sleepTimeout = SleepTimeout.NeverSleep;
    }

    // Update is called once per frame
    void Update()
    {
        // if position hasn't been set, attempt to read position data from map data
        if(!gameObjectPositionsSet)
        {
            if (ModifyTerrain.ObjectPositionData != null)
            {
                SetGameObjects(ref playerSphere, ref endBox);
                UnityEngine.Vector3 scale = playerSphere.transform.localScale;
                ClampPlayerScale(ref scale);
                gameObjectPositionsSet = true;
            }
        }

        // if the objects position haven't been set automatically, allow the user to change the position
        // TODO: Might be removed
        if (!gameObjectPositionsSet && !gameStarted)
        {
            CheckForMoveEvent();
        }
   
    }

    private void SetGameObjects(ref GameObject player, ref GameObject end)
    {
        //get midpoint coords from bounding box
        int[] eCoords = GetBoundingBoxMidPoint(
            ModifyTerrain.ObjectPositionData[0], 
            ModifyTerrain.ObjectPositionData[1], 
            ModifyTerrain.ObjectPositionData[2], 
            ModifyTerrain.ObjectPositionData[3]
            );
        //get midpoint coords from bounding box
        int[] pCoords = GetBoundingBoxMidPoint(
            ModifyTerrain.ObjectPositionData[4],
            ModifyTerrain.ObjectPositionData[5],
            ModifyTerrain.ObjectPositionData[6],
            ModifyTerrain.ObjectPositionData[7]
            );

        int[] eScale = GetBoundingBoxWidth(
            ModifyTerrain.ObjectPositionData[0],
            ModifyTerrain.ObjectPositionData[1],
            ModifyTerrain.ObjectPositionData[2],
            ModifyTerrain.ObjectPositionData[3]
            );

        int[] pScale = GetBoundingBoxWidth(
            ModifyTerrain.ObjectPositionData[4],
            ModifyTerrain.ObjectPositionData[5],
            ModifyTerrain.ObjectPositionData[6],
            ModifyTerrain.ObjectPositionData[7]
            );

        ScalePositions(ref pCoords);
        ScalePositions(ref eCoords);

        player.transform.position = new UnityEngine.Vector3(pCoords[0], 3.0f, pCoords[1]);
        player.transform.localScale = new UnityEngine.Vector3(pScale[0] * ObjectScaleFactor, pScale[0] * ObjectScaleFactor, pScale[0] * ObjectScaleFactor);
        end.transform.position = new UnityEngine.Vector3(eCoords[0], 0.0f, eCoords[1]);
        end.transform.localScale = new UnityEngine.Vector3(1.0f, eScale[0] * ObjectScaleFactor, eScale[0] * ObjectScaleFactor);
    }

    /**
     * Transforms array of positions from 0-1025 pixel space to Unity Terrain Space
     * 
     */
    private void ScalePositions(ref int[] positions)
    {
        float dim = mazeTerrain.GetComponent<Terrain>().terrainData.size.x;

        for (int i = 0; i < positions.Length; i++)
        {
            positions[i] = (int)((((float)positions[i]) * dim) / TerrainResolution);
            if (i % 2 == 1)
            {
                positions[i] = (int)(dim - positions[i]); //flip along x axis
            }
        }
    }

    /**
    * Gets the midpoint of the bounding boxes used in the image detection module
    * 
    */
    private int[] GetBoundingBoxMidPoint(int x1, int y1, int x2, int y2)
    {
        return new int[] { ((x1 + x2) / 2), ((y1 + y2) / 2) };
    }

    /**
     * Gets width of the bounding boxes from the image detection module and converts the width to terrain space
     * 
     */
    private int[] GetBoundingBoxWidth(int x1, int y1, int x2, int y2)
    {
        float dim = mazeTerrain.GetComponent<Terrain>().terrainData.size.x;
        return new int[] { (int)((x2 - x1) * dim / TerrainResolution), (int)((y2 - y1) * dim / TerrainResolution) };
    }

    private bool isTerrain(GameObject obj)
    {
        Terrain ter = obj.GetComponent<Terrain>();
        if(ter == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    //PRE-GAME OBJECT POSITIONING CODE
    /*
    This code allows the user to move the the game pieces around in the world. This code is placeholder
    until the positions of these pieces can be determined with CV.

    Allows interactivity where the user can select an item, if the item isn't terrain it's colored to display
    what is currently selected. The user can then select another location to place the object there.
    */
    private void CheckForMoveEvent()
    {
        //check if mouse is over an object when click is released
        if (Input.GetMouseButtonUp(0))
        {
            //simple click, see what was selected
            Ray ray = currentCamera.ScreenPointToRay(Input.mousePosition);
            RaycastHit hit;
            if (Physics.Raycast(ray, out hit) && (hit.collider != null))
            {
                prevSelected = selected;
                selected = hit.transform.gameObject;
                Terrain ter = selected.GetComponent<Terrain>();
                if (!isTerrain(selected) && selected == playerSphere)
                {
                    var render = selected.GetComponent<Renderer>();
                    if (selected != prevSelected)
                    {
                        prev_color = render.material.color;
                    }
                    render.material.SetColor("_Color", Color.red);
                }
                else
                {
                    if (selected == prevSelected && selected == playerSphere && prevSelected == playerSphere)
                    {
                        var render = prevSelected.GetComponent<Renderer>();
                        render.material.SetColor("_Color", prev_color);
                    }
                    if (prevSelected != null && !isTerrain(prevSelected) && prevSelected == playerSphere) //moving sphere
                    {
                        var render = prevSelected.GetComponent<Renderer>();
                        render.material.SetColor("_Color", prev_color);
                        //move the ball to where the mouse just clicked
                        prevSelected.transform.position = new UnityEngine.Vector3(hit.point.x, 3, hit.point.z);
                    }
                    else if (prevSelected != null && !isTerrain(prevSelected) && prevSelected != playerSphere)  //moving portal
                    {
                        prevSelected.transform.position = new UnityEngine.Vector3(hit.point.x, 0, hit.point.z);
                    }
                }
            }
        }
    }

    // Function that scales the player's sphere in accordance to the slider
    void OnSliderChange(Slider slider)
    {
        //scale between min, max value
        float scale = ((max_scale_value - min_scale_value) * slider.value) + min_scale_value;
        playerSphere.transform.localScale = new UnityEngine.Vector3(scale, scale, scale);
    }

    
    void ClampPlayerScale(ref UnityEngine.Vector3 scale)
    {
        // x=y=z
        float val = scale.x;
        val = Math.Min(max_scale_value, Math.Max(min_scale_value, val));
        playerSphere.transform.localScale = new UnityEngine.Vector3(val, val, val);
    }


    /**
     * Occurs when user presses the 'start' button.
     * At this point the user is expected to select their size     
     */
    void TaskOnClick()
    {
        gameStarted = true;
        gameObjectPositionsSet = true;

        // Remove drag
        var body = playerSphere.GetComponent<Rigidbody>();
        body.drag = 0;

        // Set High Score if high score is valid

        if (AuthScript.mapHighScore > 0.0)
        {
            Text htext_c = highScore.GetComponent<Text>();
            htext_c.enabled = true;
            highScore.GetComponent<Text>().text = "High Score: " + AuthScript.mapHighScore.ToString("f2");
        }

        // Start score timer
        Text text_c = score.GetComponent<Text>();
        text_c.enabled = true;
        ScoreScript script = score.GetComponent<ScoreScript>();
        script.StartScoreCounter();

        // Disable start button and slider
        startButton.gameObject.SetActive(false);
        sizeSlider.gameObject.SetActive(false);

        // Enable mid-game buttons
        restartButton.gameObject.SetActive(true);
        quitButton.gameObject.SetActive(true);

        // Enable player input
        Player p_script = playerSphere.GetComponent<Player>();
        p_script.EnableMovement();

        // Enable end box
        GameObject portal = GameObject.Find("Portal");
        EndScript endScript = portal.GetComponent<EndScript>();
        endScript.enableEndBox(true);
    }

    void RestartOnClick()
    {
        //reload the current scene
        int currentSceneIndex = SceneManager.GetActiveScene().buildIndex;
        SceneManager.LoadScene(currentSceneIndex);
    }

    void QuitOnClick()
	{
        // Quit the current game to the previous screen
        int prevSceneIndex = SceneManager.GetActiveScene().buildIndex - 1;

        if (prevSceneIndex < 0)
            prevSceneIndex = 0;

        SceneManager.LoadScene(prevSceneIndex);
    }

    public void SetRunFlag(bool flag) 
    {
        script_start_flag = flag;
    }

}
