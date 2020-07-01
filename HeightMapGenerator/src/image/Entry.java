/*
 * @author Martin Edmunds
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

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.highgui.HighGui;

import image.markup.*;


public class Entry {

	public static void main(String[] args) {
		
		//load the dll for OpenCV
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
	  	//create window  
		String window_name = "Window";
	  	HighGui.namedWindow(window_name, HighGui.WINDOW_AUTOSIZE);
		
	  	
		ImageMarkup editor;
		try {
			editor = new ImageMarkup("res/hand_maze.jpg");
		} catch (CvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to find the requested file.");
			
			return;
		}
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		
		editor.ClampSTD(-1);
		
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		
		System.exit(0);;
		
		/*
		
		//filter image
		for(int i = 0; i < 11; i++) {
			editor.Blur(9, 9);
			editor.ClampSTD(-2);
		}
		
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		
		//HighGui.imshow(window_name, editor.GetImg());
		//HighGui.waitKey();
		
		HeightMapBuilder builder = new HeightMapBuilder(editor.GetBufferedImage());
		builder.WriteHeightmap();
		
		*/
		
		//HighGui.imshow(window_name, editor.GetImg());
		//HighGui.waitKey();
		
		// TODO Auto-generated method stub
		//System.out.println("res/Test");
		//String file_name = "res/test.png";
		//HeightMapBuilder builder = new HeightMapBuilder(file_name);
		//builder.WriteHeightmap();
		

		
		//Convert Mat to BufferedImage to be used by the HeightMapBuilder
		
		/*
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		
		editor.Blur(9, 9);
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		
		editor.Sobel();
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		
		editor.Invert();
		HighGui.imshow(window_name, editor.GetImg());
		HighGui.waitKey();
		*/

		//editor.Display();
	}

}
