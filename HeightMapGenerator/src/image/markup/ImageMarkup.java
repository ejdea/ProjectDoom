/**
 * @author Martin Edmunds
 * Date: 06/30/2020
 * Project: Doodle Maze
 * 
 * Description:
 * 
 * Image is expected to be somewhat of a maze where the walls are colored black with everything else being white
 * 
 * This class is designed to filter noise and transform the raw photo into a picture that can be processed by
 * HeightMapBuilder
 * 
 * */

package image.markup;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import image.HeightMapBuilder;

import java.awt.image.*;

public class ImageMarkup {
	
	private Mat img;
	
	
	/**
	 * Constructor: attempts to load file for processing
	 * 
	 * @param filename The image file to attempt to be loaded
	 * @throws CvException On missing file
	 * */
	public ImageMarkup(String filename) throws CvException {
		this.img = Imgcodecs.imread(filename, Imgcodecs.IMREAD_GRAYSCALE);
		//resize image to 1025x1025 : resolution needed for height map
		Imgproc.resize(this.img,  this.img,  new Size(HeightMapBuilder.DEFAULT_RESOLUTION, HeightMapBuilder.DEFAULT_RESOLUTION));
		return;
	}
	
	/**
	 * Blurs the image using GaussianBlur with a (size_x, size_y) kernel
	 * 
	 * @param size_x kernel size in x dim
	 * @param size_y kernel size in y dim
	 * @return None
	 * */
	public void Blur(int size_x, int size_y) {
		if(size_x != size_y) {
			return;
		}
		else {
			if (size_x % 2 != 1 && size_y % 2 != 1) {
				return;
			}
		}
		Imgproc.GaussianBlur( this.img, this.img, new Size(size_x, size_y), 0, 0, Core.BORDER_DEFAULT );
	}
	
	/**
	 * Calculates the average pixel value of the image
	 * 
	 * 
	 * 
	 * @return double returns the pixel average of the image
	 * */
	public double GetAveragePixel() {
		double sum = 0;
		
		//copy image to temp byte array for faster processing
		int num_bytes = (int)(this.img.total() * this.img.channels());
		byte[] tmp = new byte[num_bytes];
		this.img.get(0, 0, tmp);
		
		for(int i = 0; i < num_bytes; i++) {
			sum += (byte)(tmp[i]) & 0xFF; //convert to unsigned byte (thanks java)
		}
		
		
		return (sum / (this.img.width() * this.img.height()));
	}
	
	/**
	 * Calculates the standard deviation of the pixel values in the image
	 * 
	 * 
	 * 
	 * @return double returns the pixel standard deviation of the image
	 * */
	public double GetStdDev() {
		double average = this.GetAveragePixel();
		int num_elements = this.img.width() * this.img.height();
		double pre_total = 0;
		
		//copy image to temp byte array for faster processing
		int num_bytes = (int)(this.img.total() * this.img.channels());
		byte[] tmp = new byte[num_bytes];
		this.img.get(0, 0, tmp);
		
		double values[] = new double[this.img.width() * this.img.height()];
		//Standard deviation formula
		for(int j = 0; j < this.img.width(); j++) {
			for (int i = 0; i < this.img.height(); i++) {
				double pixel_value = (byte)(tmp[(j*this.img.width()) + i]) & 0xFF;
				values[(j * this.img.width()) + i] = (pixel_value - average) * (pixel_value - average);
				pre_total += values[(j * this.img.width()) + i];
			}
		}
		
		return Math.sqrt(((double)(1.0 / num_elements) * pre_total));
	}
	
	/**
	 * Returns the current Mat image as a BufferedImage usable by the Java 2D module
	 * 
	 * @return BufferedImage Mat converted to BufferedImage
	 * */
	public BufferedImage GetBufferedImage() {
		
		BufferedImage bi_return = new BufferedImage(this.img.width(), this.img.height(), BufferedImage.TYPE_BYTE_GRAY);
		byte[] data = ((DataBufferByte) bi_return.getRaster().getDataBuffer()).getData();
		
		for(int j = 0; j < this.img.width(); j++) {
			for (int i = 0; i < this.img.height(); i++) {
				data[(j * this.img.width()) + i] = (byte)(this.img.get(j, i)[0]);
			}
		}
		
		return bi_return;
		
	}
	
	/**
	 * Default Clamp call that clamps the image to within 1 standard deviation
	 * 
	 * 
	 * @return None
	 * */
	public void ClampSTD() {
		this.ClampSTD(1);
	}
	
	/**
	 * Clamps the pixel values in the image against a value:
	 * pixels < value = 0
	 * pixels >= value = 255
	 * 
	 * 
	 * @return None
	 * */
	public void Clamp(double value) 
	{	
		int width = this.img.width();
		int height = this.img.height();
		
		//copy image to temp byte array for faster processing
		int num_bytes = (int)(this.img.total() * this.img.channels());
		byte[] tmp = new byte[num_bytes];
		this.img.get(0, 0, tmp);
		
		for(int j = 0; j < width; j++) {
			for (int i = 0; i < height; i++) {
				double pixel = (byte)((tmp[(j*width) + i])) & 0xFF;
				if(pixel < value) {
					tmp[(j * width) + i] = 0;
				}
				else {
					tmp[(j * width) + i] = (byte)255;
				}
					
			}
		}
		
		this.img.put(0, 0, tmp);
	}
	
	/**
	 * Clamps the pixel values in the image against a multiple of the standard deviation
	 * 
	 * 
	 * @return None
	 * */
	public void ClampSTD(int num_std) {
		double value = this.GetAveragePixel();
		double stdev = this.GetStdDev();
		this.Clamp(value + (stdev * num_std));
	}
	
	/**
	 * Clamps the pixel values in the image against the average pixel of the image
	 * 
	 * 
	 * @return None
	 * */
	public void ClampA() {
		double average_pixel = this.GetAveragePixel();
		this.Clamp(average_pixel);
		return;
	}
	
	/**
	 * Performs an OpenCV Sobel Line Detection function on the image
	 * 
	 * 
	 * @return None
	 * */
	public void Sobel() {
		int scale = 1;
		int delta = 0;
		int ddepth = CvType.CV_16S;
		
		//run sobel kernel
        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();
        Mat grad = new Mat();
		
		Imgproc.Sobel( this.img, grad_x, ddepth, 1, 0, 1, scale, delta, Core.BORDER_DEFAULT );
		Imgproc.Sobel( this.img, grad_y, ddepth, 0, 1, 1, scale, delta, Core.BORDER_DEFAULT );
		
        // converting back to CV_8U
        Core.convertScaleAbs( grad_x, abs_grad_x );
        Core.convertScaleAbs( grad_y, abs_grad_y );
        Core.addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad );
        this.img = grad;
	}
	
	/**
	 * Inverts the pixel values of the image
	 * 
	 * 
	 * @return None
	 * */
	public void Invert() {
		Core.bitwise_not(this.img, this.img);
	}
	
	/**
	 * Returns image height
	 * 
	 * 
	 * @return int height of the image in px
	 * */
	public int GetHeight() {
		return this.img.height();
	}
	
	/**
	 * Returns image width
	 * 
	 * 
	 * @return int width of the image in px
	 * */
	public int GetWidth() {
		return this.img.width();
	}

	/**
	 * Returns raw image data
	 * 
	 * 
	 * @return Mat OpenCV Mat class
	 * */
	public Mat GetImg() {
		return this.img;
	}
	
}
