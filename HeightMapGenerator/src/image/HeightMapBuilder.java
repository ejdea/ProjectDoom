/*
 * Author: Martin Edmunds
 * Date: 06/30/2020
 * Project: Doodle Maze
 * 
 * Description:
 * 
 * Image is expected to be somewhat of a maze where the walls are colored black with everything else being white
 * 
 * This class is designed convert an image file into a .raw file that can be imported by Unity
 * for use with terrain generation. The target heightmap file is a single channel, 16-bit, .raw
 * height resolution map.
 * 
 * */

package image;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class HeightMapBuilder {
	//(255, 255, 255) = 0xFFFFFF
	private static int white_pixel_int = -1;
	//number of bytes of depth (16 - bit)
	private static int depth = 2;
	
	private static final String default_path = "res/";
	private static final String extension = ".raw";
	private static int Counter = 2;
	
	private BufferedImage img;
	private String outputFile;
	private byte[] byte_buffer;
	

	public HeightMapBuilder(String filePath) {
		try {
			this.img = ImageIO.read(new File(filePath));
			
			int width = this.img.getWidth();
			int height = this.img.getHeight();
			
			this.byte_buffer = new byte[width * height * depth];
			this.GenerateMap();
			
			this.outputFile = filePath.substring(0, filePath.indexOf("."));
			this.outputFile = outputFile.concat(HeightMapBuilder.extension);
			HeightMapBuilder.Counter++;
		}
		catch (IOException e){
			System.out.println("Could not find the file");
			return;
		}
	}
	

	public HeightMapBuilder(BufferedImage img) {
		this.img = img;
		
		int width = this.img.getWidth();
		int height = this.img.getHeight();
		
		this.byte_buffer = new byte[width * height * depth];
		this.GenerateMap();
		
		this.outputFile = HeightMapBuilder.default_path + "hmap" + Integer.toString(HeightMapBuilder.Counter) + HeightMapBuilder.extension;
		HeightMapBuilder.Counter++;
	}
	
	
	public void GenerateMap() {
		int byte_index = 0;
		for(int j = 0; j < img.getWidth(); j++) {
			for(int i = 0; i < img.getHeight(); i++) {
				
				// -1 = white = heightmap = 0x0100 0x01, 0x00
				// else = black = heightmap = 0x0000 0x00, 0x00
				if(img.getRGB(j, i) == white_pixel_int) {
					this.byte_buffer[byte_index] = 0x00;
					this.byte_buffer[byte_index + 1] = 0x00;
				}
				else {
					this.byte_buffer[byte_index] = 0x00;
					this.byte_buffer[byte_index + 1] = 0x01;
				}
				byte_index += depth;
			}
		}
	}
	
	
	public byte[] GetHeightmap() {
		return this.byte_buffer;
	}
	
	
	public void WriteHeightmap(String file_location)
	{
		try(OutputStream out = new FileOutputStream(file_location)){
			
			for(int i = 0; i < this.byte_buffer.length; i++) {
				out.write(byte_buffer[i]);
			}
		}
		catch(IOException ex) {
			System.out.println("Could not open location to write to");
		}
	}
	
	
	public void WriteHeightmap() {
		this.WriteHeightmap(this.outputFile);
	}
	
	

}
