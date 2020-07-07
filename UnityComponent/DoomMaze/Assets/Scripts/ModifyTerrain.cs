using System.Collections;
using System.Collections.Generic;
using System;
using System.IO;
using UnityEngine;
using System.Security.Cryptography;

public class ModifyTerrain : MonoBehaviour
{

    public GameObject TerrainObj;
    public Material default_mat;
    private TerrainData _TerrainData;

    /*
     Read a binary file and return an array with the read data
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
    Builds a height map from the binary data passed from ReadStreamingAssets, or ReadBytes
    */
    void BuildMap(byte[] b_data)
    {
        int h = _TerrainData.heightmapResolution;
        int w = _TerrainData.heightmapResolution;
        float[] f_data = new float[h * w];
        float[,] data = new float[h, w];
        int k = 0;
        
        //combine 16-bit length values into a float between 0 - 1
        for (int i = 0; i < b_data.Length; i += 2)
        {
            var d = 0;
            d += (b_data[i + 1] << 8);
            d = d | (b_data[i]);
            f_data[k] = (float)d / 0xFFFF;
            k++;
        }

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
     https://docs.unity3d.com/ScriptReference/Networking.UnityWebRequest.html
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
