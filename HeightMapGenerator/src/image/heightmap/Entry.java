package image.heightmap;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.highgui.HighGui;

import image.markup.*;

/**
 * Example Class Only
 * 
 * @author Martin Edmunds
 * @version 1.0
 * @since 2020-07-01
 * 
 * */
public class Entry {

	public static void main(String[] args) {
		
		//load the dll for OpenCV
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
	  	//create window  
		String window_name = "Window";
	  	HighGui.namedWindow(window_name, HighGui.WINDOW_AUTOSIZE);
		
	  	/*
	  	 * Example Usage
	  	 * */
	  	ImageMarkup img;
		try {
			img = new ImageMarkup("res/hand_maze.jpg");
		} catch (CvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to find the requested file.");
			return;
		}
		HighGui.imshow(window_name, img.GetImg());
		HighGui.waitKey();
		
		//filter the image with 10 iterations
		img.Filter(5);
		
		HighGui.imshow(window_name, img.GetImg());
		HighGui.waitKey();
		
		//convert the image to a heightmap
		HeightMap map;
		try {
			map = new HeightMap(img.GetBufferedImage());
			map.Write();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to write the height map.");
		}
		
		System.out.println("Done.");
		System.exit(0);
		
	}

}
