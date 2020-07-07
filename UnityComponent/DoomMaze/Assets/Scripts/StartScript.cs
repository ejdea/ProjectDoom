using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Security.Cryptography;
using UnityEngine;
using UnityEngine.UI;

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
    private bool player_selected = false;
    private bool box_selected = false;
    private bool script_start_flag = false;

    Color prev_color;

    // Start is called before the first frame update
    void Start()
    {

        //add listener to startButton
        startButton.onClick.AddListener(TaskOnClick);
        Text text_c = score.GetComponent<Text>();
        text_c.enabled = false;

        //start slider with half value
        sizeSlider.value = 0.5f;
        OnSliderChange(sizeSlider);

        //bind slider value change method
        sizeSlider.onValueChanged.AddListener(delegate
        {
            OnSliderChange(sizeSlider);
        });

        //ensure that the ball doesn't move too much when the user is placing it
        var body = playerSphere.GetComponent<Rigidbody>();
        body.drag = 100;

    }

    // Update is called once per frame
    void Update()
    {
        //PRE-GAME OBJECT POSITIONING CODE
        //while the game hasn't started, allow the user to move around the pieces in the world
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
                    if(!isTerrain(selected))//selected != mazeTerrain)
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
                        if(prev_selected != null && !isTerrain(prev_selected))//prev_selected != mazeTerrain)
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

    /*
     Will need to be modified for mobile port
     */
    private Ray GetRay()
    {
        RaycastHit hit;
        Ray ray = current_camera.ScreenPointToRay(Input.mousePosition);
        return ray;
    }


    void OnSliderChange(Slider slider)
    {
        Transform t = playerSphere.GetComponent<Transform>();
        float new_scale = 1 + (slider.value * scale_factor);
        t.localScale = new Vector3(new_scale, new_scale, new_scale);
    }

    /*
    Occurs when user presses the 'start' button.
    At this point the user is expected to select their size
     
     */
    void TaskOnClick()
    {
        game_started = true;

        //remove drag
        var body = playerSphere.GetComponent<Rigidbody>();
        body.drag = 0;

        //start score timer
        Text text_c = score.GetComponent<Text>();
        text_c.enabled = true;
        ScoreScript script = score.GetComponent<ScoreScript>();
        script.StartScoreCounter();

        //disable start button and slider
        startButton.gameObject.SetActive(false);
        sizeSlider.gameObject.SetActive(false);

        //enable player input
        Player p_script = playerSphere.GetComponent<Player>();
        p_script.EnableMovement();
    }


    public void SetRunFlag(bool flag) 
    {
        script_start_flag = flag;
    }

}
