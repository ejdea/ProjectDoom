package com.doomteam.doodlemaze;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


/**
 * Class used to encapsulate all necessary data needed for Unity terrain generation and game state
 *
 * @author Martin Edmunds
 * @since 2020-07-31
 * @version 1.3
 */
public class LevelData {

    /**
     * Class utility function to convert a list of integer positions into a byte array
     *
     * @param arr list of integers to be converted into bytes
     * @return byte representation of the integers
     * */
    public static byte[] ConvertIntsToBytes(List<Integer> arr){
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

    /**
     * Class utility function to convert serialized positions into ArrayList
     *
     * @param data byte representation of integers
     * @return ArrayList of position data
     * */
    public static ArrayList<Integer> ConvertBytesToInts(byte[] data){
        ArrayList<Integer> to_return = new ArrayList<>();
        for(int i = 0; i < HEIGHTDATA_OFFSET; i += 4){
            int val = 0;
            val |= data[i] & 0xFF;
            val = val << 8;
            val |= data[i+1] & 0xFF;
            val = val << 8;
            val |= data[i+2] & 0xFF;
            val = val << 8;
            val |= data[i+3] & 0xFF;
            to_return.add(val);
        }
        return to_return;
    }
    private static int HEIGHTDATA_OFFSET = 32;
    private static int HEIGHTMAP_RESOLUTION = 1025;

    // composite level data, consists of 32 bytes of obj positions + 1025 * 1025 * 2 bytes of heightmap data
    private byte[] m_data;
    private ArrayList<Integer> m_objPositions;

    /**
     * Constructor used to build level data from local storage
     *
     * @param filePath string file path
     * @param filename string file name
     * @throws IOException of fileIO error
     * */
    LevelData(String filePath, String filename) throws IOException {
        File file = new File(filePath, filename);
        m_data = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        in.read(m_data);
        m_objPositions = LevelData.ConvertBytesToInts(m_data);
    }

    /**
     * Constructor used to build level data from heightmap and object position list
     *
     * @param heightData 1025x1025 heightmap from ImageMarkup
     * @param objPositions ArrayList of bounding box coords
     * */
    LevelData(byte[] heightData, List<Integer> objPositions)
    {
        m_objPositions = new ArrayList<>(objPositions);
        m_data = new byte[heightData.length + (objPositions.size() * 4)];
        byte[] sPositionData = LevelData.ConvertIntsToBytes(m_objPositions);
        // copy n bytes from position data into level data buffer
        for(int i = 0; i < sPositionData.length; i++)
        {
            m_data[i] = sPositionData[i];
        }
        // copy m bytes from heightmap data into level data buffer
        for(int i = 0; i < heightData.length; i++)
        {
            m_data[i + HEIGHTDATA_OFFSET] = heightData[i];
        }
    }

    /**
     * Constructor used to build level data from heightmap and object position list
     *
     * @param data n + m byte buffer consisting of 32 bytes position data + heightmap data
     * */
    LevelData(byte[] data)
    {
        m_data = data;
        m_objPositions = LevelData.ConvertBytesToInts(data);
    }

    /**
     * Getter for level data
     *
     * @return byte array consisting of 32 bytes positioning data and 1025x1025x2 bytes heightmap data
     * */
    byte[] GetData()
    {
        return m_data;
    }

    /**
     * Getter for position data
     *
     * @return ArrayList of bounding box coords for position data
     * */
    ArrayList<Integer> GetPositions()
    {
        return m_objPositions;
    }

    /**
     * Converts the currently held heightmap into a bitmap image that can be used for display
     *
     * @return Bitmap representation of the heightmap
     * */
    Bitmap GetImage()
    {
        int num_pixels = HEIGHTMAP_RESOLUTION * HEIGHTMAP_RESOLUTION;
        int[] pixelData = new int[num_pixels];
        int idxCounter = 0;
        for(int i = HEIGHTDATA_OFFSET; i < m_data.length; i += 2)
        {
            int val = 0;
            val |= m_data[i];
            val <<= 2;
            val |= m_data[i+1];
            if(val == 0) {
                //white
                pixelData[idxCounter] = 0xFFFFFFFF;
            }
            else {
                //black
                pixelData[idxCounter] = 0xFF000000;
            }
            idxCounter++;
        }

        return Bitmap.createBitmap(pixelData, HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION, Bitmap.Config.ARGB_8888);
    }

    /**
     * Function used to save the currently held level data as a file
     *
     * @param filepath location to save map data
     * @param filename name of the file to be saved as
     * @throws IOException on IO error
     * */
    void Save(String filepath, String filename) throws IOException {
        File file = new File(filepath, filename);
        FileOutputStream out = new FileOutputStream(file);
        out.write(m_data);
    }

    /**
     * Function used to get a unique hash for the map to use as a key in the DB
     *
     * @return String representation of hash
     * */
    String GetHash(){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(m_data);
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                //convert byte to unsigned value, get hex string
                String h = Integer.toHexString(0xFF & aMessageDigest);
                //force hex bytes to be at least two characters wide with leading zero (ie. 'b' -> '0b')
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            return "";
        }
    }


}
