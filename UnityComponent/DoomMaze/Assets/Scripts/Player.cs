using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Threading;
using UnityEngine;

public class Player : MonoBehaviour
{
    public GameObject m_sphere;
    public Rigidbody m_body;

    //default speed value that gives decent control over the ball
    private float m_speed = 1000.0f;

    //additional down force to ensure ball doesnt go flying
    private float down_force = -5.0f;

    //factor to speed up the mobile sensor
    private float accel_factor = 5.0f;

    //flag to ensure movement doesn't occur until the game actually starts
    bool input_enabled = false;

    // Start is called before the first frame update
    void Start()
    {
    }

    void SetPosition(float x, float y)
    {
    }

    public void EnableMovement()
    {
        input_enabled = true;
    }

    public void DisableMovement()
    {
        input_enabled = false;
    }

    void Move()
    {
#if UNITY_ANDROID
        //get input from accelerometer
        Vector3 movement = new Vector3 (Input.acceleration.x, down_force, Input.acceleration.y);
        float force = m_speed * Time.deltaTime * accel_factor;

        m_body.AddForce(movement * force);

#elif UNITY_EDITOR
        //get input from input manager
        float verticle_force = Input.GetAxis("Vertical") * m_speed;
        float horizontal_force = Input.GetAxis("Horizontal") * m_speed;

        verticle_force *= Time.deltaTime;
        horizontal_force *= Time.deltaTime;

        //apply input
        m_body.AddForce(Vector3.forward * verticle_force);
        m_body.AddForce(Vector3.right * horizontal_force);
#else
        //get input from input manager
        float verticle_force = Input.GetAxis("Vertical") * m_speed;
        float horizontal_force = Input.GetAxis("Horizontal") * m_speed;

        verticle_force *= Time.deltaTime;
        horizontal_force *= Time.deltaTime;

        //apply input
        m_body.AddForce(Vector3.forward * verticle_force);
        m_body.AddForce(Vector3.right * horizontal_force);
#endif
    }

    // Update is called once per frame
    void Update()
    {
        if (input_enabled) 
        {
            Move();
        }
    }

}
