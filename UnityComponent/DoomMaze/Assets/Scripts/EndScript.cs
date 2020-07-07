using System.CodeDom;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class EndScript : MonoBehaviour
{

    private bool should_game_end = false;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    // Set the end flag to ensure that end game code won't be ran prematurely
    public void SetEndFlag(bool value)
    {
        should_game_end = value;
    }

    /*
     For some reason unity triggers this on load. I've wrapped the event with a flag that can be set
     to 'true' after the game officially starts.
     */
    private void OnTriggerEnter(Collider other)
    {
        if(should_game_end == true)
        {
            UnityEngine.Debug.Log("End game!!!");
        }
    }

}
