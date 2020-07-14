/*
 * Authors: Martin Edmunds, Edmund Dea, Lee Rice
 * Project: Project Doom
 * Date: 07/07/2020
 * Version: 1.0
 */

using System.Collections;
using System.Collections.Generic;
using System;
using System.IO;
using UnityEngine;
using System.Security.Cryptography;

/*
 * Class that builds a terrain from a heightmap that's stored in the Assets/StreamingAssets folder
 */
public class ModifyTerrain : MonoBehaviour
{

    public GameObject TerrainObj;
    public Material default_mat;
    private TerrainData _TerrainData;

    /*
     * Read a binary file and return an array with the read data
     */
    byte[] ReadBytes(string aFileName)
    {
        byte[] to_return = null;
        try
        {
            to_return = System.IO.File.ReadAllBytes(aFileName);
        }
        catch
        {
            UnityEngine.Debug.Log("Failed to load terrain file");
        }
        return to_return;
    }

    /*
     * Builds a height map from the binary data passed from ReadStreamingAssets, or ReadBytes
     */
    void BuildMap(byte[] b_data)
    {
        int h = _TerrainData.heightmapResolution;
        int w = _TerrainData.heightmapResolution;
        //1D tmp float data
        float[] f_data = new float[h * w];
        //2D float data to be passed to Terrain.SetHeights()
        float[,] data = new float[h, w];
        
        int k = 0;
        //combine 16-bit length values into a float between 0 - 1
        for (int i = 0; i < b_data.Length; i += 2)
        {
            var d = 0;
            d += (b_data[i + 1] << 8);      //add first 8 bits of unsigned data (ex: 0xAB) (Little Endian Order)
            d = d | (b_data[i]);            //add second 8 bits of unsigned data (ex: 0x11) (Little Endian Order) (d = 0xAB11)
            f_data[k] = (float)d / 0xFFFF;  //convert unsigned short into 0 - 1 float value by dividing by max unsigned short
            k++;
        }
        
        //Build 2D float data from 1D tmp array
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                data[y, x] = f_data[(y * w) + x];
            }
        }

        _TerrainData.SetHeights(0, 0, data);
        _TerrainData.size = new Vector3(100, 600, 100);

    }

    /*
     * Coroutine that reads a file from the stored jar file path located at Assets/StreamingAssets
     * https://docs.unity3d.com/ScriptReference/Networking.UnityWebRequest.html
     */
    IEnumerator ReadFromStreamingAssets(string file_name)
    {
        string filePath = System.IO.Path.Combine(Application.streamingAssetsPath, file_name);
        byte[] result;
        if (filePath.Contains("://") || filePath.Contains(":///"))
        {
            //read jar file path
            UnityEngine.Networking.UnityWebRequest www = UnityEngine.Networking.UnityWebRequest.Get(filePath);
            yield return www.SendWebRequest();
            result = www.downloadHandler.data;
        }
        else
        {
            //read like a normal filepath
            result = System.IO.File.ReadAllBytes(filePath);
        }


        BuildMap(result);

        //write to Android/data/com.company.DoomMaze/files (copies the file)
        File.WriteAllBytes(Application.persistentDataPath + "/" + file_name, result);
    }


    // Start is called before the first frame update
    void Start()
    {

        _TerrainData = new TerrainData();
        _TerrainData.heightmapResolution = 1024;
        _TerrainData.baseMapResolution = 1024;
        _TerrainData.SetDetailResolution(1024, 32);

        int _heightmapWidth = _TerrainData.heightmapResolution;
        int _heightmapHeight = _TerrainData.heightmapResolution;

        TerrainCollider _TerrainCollider = TerrainObj.GetComponent<TerrainCollider>();
        Terrain _Terrain2 = TerrainObj.GetComponent<Terrain>();

        _TerrainCollider.terrainData = _TerrainData;
        _Terrain2.terrainData = _TerrainData;

        //path to read data from
        string path = Path.Combine(Application.streamingAssetsPath + "/mobile_height_map.raw");

        StartCoroutine(ReadFromStreamingAssets("mobile_height_map.raw"));

    }

    // Update is called once per frame
    void Update()
    {

    }
}
