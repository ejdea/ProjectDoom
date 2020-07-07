using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using UnityEngine;
using UnityEngine.UI;

public class ScoreScript : MonoBehaviour
{
    public GameObject text_parent;
    Text score_text;

    float current_time = 0;
    
    bool game_started = false;
    public string score_string = "Text(s): ";

    // Start is called before the first frame update
    void Start()
    {
        score_text = GetComponent<Text>();
    }

    // Update is called once per frame
    void Update()
    {
        if (game_started)
        {
            current_time += Time.deltaTime;
            score_text.text = score_string + current_time.ToString("f2");
        }
    }

    public void Reset()
    {
        this.game_started = false;
        current_time = 0;
    }

    public void StartScoreCounter()
    {
        this.game_started = true;
    }

    public float GetTime()
    {
        return current_time;
    }

}
