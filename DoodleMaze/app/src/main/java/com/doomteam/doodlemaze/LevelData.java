package com.doomteam.doodlemaze;

import android.net.Uri;
import java.util.List;

public class LevelData {

    private byte[] m_data;

    /**
     * Class utility function to convert a list of integer positions into a byte array
     *
     * @param arr list of integers to be converted into bytes
     * @return byte representation of the integers
     * */
    public static byte[] GetPositionBytes(List<Integer> arr){
        byte[] to_return = new byte[arr.size() * 4];
        int byte_counter = 0;
        for(int i = 0; i < arr.size(); i++){
            to_return[byte_counter++] = (byte)(arr.get(i) >> 24);
            to_return[byte_counter++] = (byte)((arr.get(i) & 0x00FFFFFF) >> 16);
            to_return[byte_counter++] = (byte)((arr.get(i) & 0x0000FFFF) >> 8);
            to_return[byte_counter++] = (byte)((arr.get(i) & 0x000000FF));
        }

        return to_return;
    }

    LevelData(Uri uri)
    {

    }

    LevelData(String filename)
    {

    }

    LevelData(byte[] leveldata)
    {

    }

    LevelData(byte[] heightdata, List<Integer> objPositions)
    {

    }


    byte[] GetData()
    {
        return null;
    }



}
