/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

//#define DYNAMICALLY_CLAMP_POSITION

using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Threading;
using UnityEngine;

/*
 * Class that controls the player object
 */
public class Player : MonoBehaviour
{
    public GameObject m_sphere;
    public Rigidbody m_body;

    // Default speed value that gives decent control over the ball
    private const float m_speed = 3000.0f;

    // Maximum velocity of the ball
    private const float maxVelocity = 80.0f;

#if UNITY_ANDROID
    // Additional down force to ensure ball doesnt go flying
    private float down_force = -50.0f;

    // Factor to speed up the mobile sensor
    private float accel_factor = 40.0f;
#endif

    //flag to ensure movement doesn't occur until the game actually starts
    bool input_enabled = false;

    private Camera mazeCamera;
    private float ballWidth, ballHeight;
    private UnityEngine.Vector3 prevPosition;
    private AudioSource audioSource;
    private Rigidbody ballRb;

    GameObject scriptHolder;
    private AudioSource bgMusicAudioSource;
    [SerializeField] AudioClip[] bgMusic = null;

    // Start is called before the first frame update
    void Start()
    {
        audioSource = GetComponent<AudioSource>();
        ballRb = GetComponent<Rigidbody>();
        scriptHolder = GameObject.FindWithTag("ScriptHolder");
        bgMusicAudioSource = scriptHolder.GetComponent<AudioSource>();

        // Disable screen dimming
        Screen.sleepTimeout = SleepTimeout.NeverSleep;
    }

    private void playBackgroundMusic()
	{
        if (input_enabled && !bgMusicAudioSource.isPlaying)
		{
            // Play background music
            int bgMusicIndex = UnityEngine.Random.Range(0, bgMusic.Length);
            AudioClip clip = bgMusic[bgMusicIndex];
            bgMusicAudioSource.PlayOneShot(clip);
        }
    }

    public void SetPosition(float x, float z)
    {
        UnityEngine.Vector3 pos = new UnityEngine.Vector3(x, transform.position.y, z);
        transform.position = pos;
    }

    public void EnableMovement()
    {
        input_enabled = true;
        ballRb.isKinematic = false;
    }

    public void DisableMovement()
    {
        input_enabled = false;
        ballRb.isKinematic = true;
    }

    void Move()
    {
        // Get camera object
        if (!mazeCamera)
        {
            mazeCamera = Camera.current;

            // Set ball width and height
            // Note: Ideally, ballWidth and ballHeight should be 1/2 the
            // x or z value. Not sure why using 1/1 looks better.
            Renderer ballSprite = transform.GetComponent<Renderer>();
            ballWidth = ballSprite.bounds.size.x;
            ballHeight = ballSprite.bounds.size.z;

#if DYNAMICALLY_CLAMP_POSITION
			/* 
             * Clamp ball position to be within main camera's view
			 * WIP - Right now, camBottomLeft and camTopRight don't return the correct values. For now, using static values to clamp ball position.
             */
			UnityEngine.Vector3 camBottomLeft = mazeCamera.ScreenToWorldPoint(UnityEngine.Vector3.zero);
            UnityEngine.Vector3 camTopRight = mazeCamera.ScreenToWorldPoint(new UnityEngine.Vector3(mazeCamera.pixelHeight, mazeCamera.pixelWidth));
            mazeCameraRect = new Rect(camBottomLeft.x, camBottomLeft.z, camTopRight.x - camBottomLeft.x, camTopRight.y - camBottomLeft.y);
#endif
        }

#if DYNAMICALLY_CLAMP_POSITION
        float clampX = Mathf.Clamp(transform.position.x, mazeCameraRect.xMin + ballWidth, mazeCameraRect.xMax - ballWidth);
        float clampY = Mathf.Clamp(transform.position.y, ballHeight / 2, ballHeight / 2);
        float clampZ = Mathf.Clamp(transform.position.z, mazeCameraRect.yMin + ballHeight, mazeCameraRect.yMax - ballHeight);
#else
        // Restrict player from moving out of the camera view and from jumping over walls
        float maxHeight = ballHeight / 2;
        float clampX = Mathf.Clamp(transform.position.x, -40.0f + ballWidth, 140.0f - ballWidth);
        float clampY = Mathf.Clamp(transform.position.y, maxHeight, maxHeight);
        float clampZ = Mathf.Clamp(transform.position.z, 3.5f + ballHeight, 96.5f - ballHeight);
#endif
        transform.position = new UnityEngine.Vector3(clampX, clampY, clampZ);

#if UNITY_ANDROID
        //get input from accelerometer
        UnityEngine.Vector3 movement = new UnityEngine.Vector3(Input.acceleration.x, down_force, Input.acceleration.y);
        float force = m_speed * Time.deltaTime * accel_factor;

        m_body.AddForce(movement * force);

#elif UNITY_EDITOR
        //get input from input manager
        float vertical_force = Input.GetAxis("Vertical") * m_speed;
        float horizontal_force = Input.GetAxis("Horizontal") * m_speed;

        vertical_force *= Time.deltaTime;
        horizontal_force *= Time.deltaTime;

        //apply input
        m_body.AddForce(UnityEngine.Vector3.forward * vertical_force);
        m_body.AddForce(UnityEngine.Vector3.right * horizontal_force);
        m_body.AddForce(Physics.gravity * m_body.mass);
#else
        // Get input from input manager
        float vertical_force = Input.GetAxis("Vertical") * m_speed;
        float horizontal_force = Input.GetAxis("Horizontal") * m_speed;

        vertical_force *= Time.deltaTime;
        horizontal_force *= Time.deltaTime;

        // Apply input
        m_body.AddForce(Vector3.forward * vertical_force);
        m_body.AddForce(Vector3.right * horizontal_force);
#endif
        // Set maximum ball velocity
        ballRb.velocity = UnityEngine.Vector3.ClampMagnitude(ballRb.velocity, maxVelocity);

        // Play audio for rolling ball
        float velocityDelta = ballRb.velocity.magnitude / maxVelocity;
        audioSource.volume = velocityDelta;

        if (transform.position.x != prevPosition.x ||
            transform.position.z != prevPosition.z)
        {
            if (!audioSource.isPlaying)
			{
                audioSource.Play();
            }
        }
		else
		{
            audioSource.Stop();
        }

        prevPosition = transform.position;
    }

    // Update is called every fixed framerate frame
    private void FixedUpdate()
    {
        if (input_enabled)
        {
            Move();
            playBackgroundMusic();
        }
    }
}
