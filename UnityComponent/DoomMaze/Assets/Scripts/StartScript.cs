/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Security.Cryptography;
using UnityEngine;
using UnityEngine.UI;

/*
 * Script to control the start up sequence of the game 
 * IN-DEVELOPMENT
 */
public class StartScript : MonoBehaviour
{
    public Button startButton;
    public Slider sizeSlider;
    public GameObject score;
    public GameObject playerSphere;
    public GameObject mazeTerrain;
    public Camera current_camera;

    public GameObject selected;
    public GameObject prev_selected;

    private float scale_factor = 5;

    private bool game_started = false;
    private bool script_start_flag = false;
    Color prev_color;

    // Start is called before the first frame update
    void Start()
    {
        // Add listener to startButton
        startButton.onClick.AddListener(TaskOnClick);
        Text text_c = score.GetComponent<Text>();
        text_c.enabled = false;

        // Start slider with half value
        sizeSlider.value = 0.5f;
        OnSliderChange(sizeSlider);

        // Bind slider value change method
        sizeSlider.onValueChanged.AddListener(delegate
        {
            OnSliderChange(sizeSlider);
        });

        // Ensure that the ball doesn't move too much when the user is placing it
        var body = playerSphere.GetComponent<Rigidbody>();
        body.drag = 100;
    }

    // Update is called once per frame
    void Update()
    {
        //PRE-GAME OBJECT POSITIONING CODE
        /*
        This code allows the user to move the the game pieces around in the world. This code is placeholder
        until the positions of these pieces can be determined with CV.
        
        Allows interactivity where the user can select an item, if the item isn't terrain it's colored to display
        what is currently selected. The user can then select another location to place the object there.
        */
        if (!game_started && script_start_flag)
        {
            //check if mouse is over an object when click is released
            if (Input.GetMouseButtonUp(0))
            {
                //simple click, see what was selected
                Ray ray = GetRay();
                RaycastHit hit;
                if (Physics.Raycast(ray, out hit) && (hit.collider != null))
                {
                    prev_selected = selected;
                    selected = hit.transform.gameObject;
                    Terrain ter = selected.GetComponent<Terrain>();
                    if(!isTerrain(selected))
                    {
                        var render = selected.GetComponent<Renderer>();
                        if(selected != prev_selected)
                        {
                            prev_color = render.material.color;
                        }
                        render.material.SetColor("_Color", Color.red);
                    }
                    else
                    {
                        if(prev_selected != null && !isTerrain(prev_selected))
                        {
                            var render = prev_selected.GetComponent<Renderer>();
                            render.material.SetColor("_Color", prev_color);
                            //move the ball to where the mouse just clicked
                            var new_pos = prev_selected.GetComponent<Transform>();
                            new_pos.position = new Vector3(hit.point.x, 3, hit.point.z);
                        }
                    }
                }
            }
        }
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

    private Ray GetRay()
    {
        Ray ray = current_camera.ScreenPointToRay(Input.mousePosition);
        return ray;
    }

    // Function that scales the player's sphere in accordance to the slider
    void OnSliderChange(Slider slider)
    {
        Transform t = playerSphere.GetComponent<Transform>();
        //allow user to scale ball between (1 - scale_factor)
        float new_scale = 1 + (slider.value * scale_factor);
        t.localScale = new Vector3(new_scale, new_scale, new_scale);
    }

    /* 
     * Occurs when user presses the 'start' button.
     * At this point the user is expected to select their size     
     */
    void TaskOnClick()
    {
        game_started = true;

        // Remove drag
        var body = playerSphere.GetComponent<Rigidbody>();
        body.drag = 0;

        // Start score timer
        Text text_c = score.GetComponent<Text>();
        text_c.enabled = true;
        ScoreScript script = score.GetComponent<ScoreScript>();
        script.StartScoreCounter();

        // Disable start button and slider
        startButton.gameObject.SetActive(false);
        sizeSlider.gameObject.SetActive(false);

        // Get start position of the ball
        Vector2 ballStartPos = new Vector2(25.0f, 50.0f);

        // Set start position of the ball
        Player p_script = playerSphere.GetComponent<Player>();
        //p_script.SetPosition(ballStartPos.x, ballStartPos.y);

        // Enable player input
        p_script.EnableMovement();

        // Enable end box
        GameObject portal = GameObject.Find("Portal");
        EndScript endScript = portal.GetComponent<EndScript>();
        endScript.enableEndBox(true);
    }


    public void SetRunFlag(bool flag) 
    {
        script_start_flag = flag;
    }

}
