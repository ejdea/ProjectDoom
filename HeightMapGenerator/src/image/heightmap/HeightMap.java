package image.heightmap;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

/**
 * Convert 1-channel grey scale image to a 16-bit 1-channel raw heightmap
 * 
 * This class is designed convert an image file into a .raw file that can be imported by Unity
 * for use with terrain generation. The target heightmap file is a single channel, 16-bit, .raw
 * height resolution map.
 * 
 * <b>Image is expected to be somewhat of a maze where the walls are colored black with everything else being white</b>
 * 
 * @author Martin Edmunds
 * @version 1.0
 * @since 2020-07-01
 * 
 * */
public class HeightMap {
	public static int DEFAULT_RESOLUTION = 1025;
	
	private static int white_pixel_int = -1;
	//number of bytes of depth (16 - bit)
	private static int depth = 2;
	
	private static final String default_path = "res/";
	private static final String extension = ".raw";
	private static int Counter = 0;
	
	private BufferedImage img;
	private String outputFile;
	private byte[] byte_buffer;
	
	/**
	 * Constructor: attempts to load file for processing
	 * 
	 * @param filename The image file to attempt to be loaded
	 * @throws IOException on missing file
	 * @throws IndexOutOfBoundsException on heightmap conversion error
	 * */
	public HeightMap(String filename) throws IOException, IndexOutOfBoundsException {
		this.img = ImageIO.read(new File(filename));
		
		int width = this.img.getWidth();
		int height = this.img.getHeight();
		
		this.byte_buffer = new byte[width * height * depth];
		this.GenerateMap();
		
		//replace filename.ext with filename.raw
		this.outputFile = filename.substring(0, filename.indexOf("."));
		this.outputFile = outputFile.concat(HeightMap.extension);
		HeightMap.Counter++;
	}
	
	/**
	 * Constructor: attempts to build heightmap directly from a buffered image
	 * 
	 * @param img BufferedImage to be converted to heightmap
	 * @throws IndexOutOfBoundsException on heightmap conversion error
	 * @throws NullPointerException on invalid img
	 * */
	public HeightMap(BufferedImage img) throws IndexOutOfBoundsException, NullPointerException{
		this.img = img;
		if(this.img == null) {
			throw new NullPointerException();
		}
		
		int width = this.img.getWidth();
		int height = this.img.getHeight();
		
		this.byte_buffer = new byte[width * height * depth];
		this.GenerateMap();
		
		this.outputFile = HeightMap.default_path + "hmap" + Integer.toString(HeightMap.Counter) + HeightMap.extension;
		HeightMap.Counter++;
	}
	
	/**
	 * Constructs the heightmap of the image. Format of the heightmap is a 16-bit 1-channel raw file. 
	 * 
	 * 
	 * 
	 * */
	public void GenerateMap() throws IndexOutOfBoundsException{
		int byte_index = 0;
		for(int j = 0; j < img.getWidth(); j++) {
			for(int i = 0; i < img.getHeight(); i++) {
				
				// -1 = white = heightmap = 0x0100 0x01, 0x00
				// else = black = heightmap = 0x0000 0x00, 0x00
				this.byte_buffer[byte_index] = 0x00;
				if(img.getRGB(j, i) == white_pixel_int) {
					this.byte_buffer[byte_index + 1] = 0x00;
				}
				else {
					this.byte_buffer[byte_index + 1] = 0x01;
				}
				byte_index += depth;
			}
		}
	}
	
	/**
	 * Returns raw byte heightmap
	 * 
	 * 
	 * @return byte[] heightmap raw
	 * */
	public byte[] GetHeightmap() {
		return this.byte_buffer;
	}
	
	/**
	 * Writes the heightmap to a file location
	 * 
	 * @param file_location location of the file to be written
	 * @throws IOException file path not valid or error while writing
	 * */
	public void Write(String file_location) throws IOException
	{
		OutputStream out = new FileOutputStream(file_location);
			
		for(int i = 0; i < this.byte_buffer.length; i++) {
			out.write(byte_buffer[i]);
		}
		out.close();
	}
	
	/**
	 * Writes the heightmap to a default file location
	 * 
	 * 
	 * @throws IOException file path not valid or error while writing
	 * */
	public void Write() throws IOException {
		this.Write(this.outputFile);
	}
	
	

}
