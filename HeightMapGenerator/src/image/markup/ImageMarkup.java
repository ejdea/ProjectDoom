/*
 * Author: Martin Edmunds
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
import java.awt.image.*;

public class ImageMarkup {
	
	private Mat img;
	
	public ImageMarkup(String filename) {
		try {
			this.img = Imgcodecs.imread(filename, Imgcodecs.IMREAD_GRAYSCALE);
		}
		catch(Exception e){
			return;
		}
		//resize image to 800x800
		Imgproc.resize(this.img,  this.img,  new Size(2049, 2049));	
		return;
	}
	
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
	
	public double GetAveragePixel() {
		double sum = 0;
		
		int num_bytes = (int)(this.img.total() * this.img.channels());
		byte[] tmp = new byte[num_bytes];
		this.img.get(0, 0, tmp);
		
		for(int i = 0; i < num_bytes; i++) {
			sum += (byte)(tmp[i]) & 0xFF;
		}
		
		
		return (sum / (this.img.width() * this.img.height()));
	}
	
	public double GetStdDev() {
		double average = this.GetAveragePixel();
		int num_elements = this.img.width() * this.img.height();
		double pre_total = 0;
		
		int num_bytes = (int)(this.img.total() * this.img.channels());
		byte[] tmp = new byte[num_bytes];
		this.img.get(0, 0, tmp);
		
		double values[] = new double[this.img.width() * this.img.height()];
		for(int j = 0; j < this.img.width(); j++) {
			for (int i = 0; i < this.img.height(); i++) {
				double pixel_value = (byte)(tmp[(j*this.img.width()) + i]) & 0xFF;
				values[(j * this.img.width()) + i] = (pixel_value - average) * (pixel_value - average);
				pre_total += values[(j * this.img.width()) + i];
			}
		}
		
		return Math.sqrt(((double)(1.0 / num_elements) * pre_total));
	}
	
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
	
	public void ClampSTD() {
		this.ClampSTD(1);
	}
	
	public void Clamp(double value) 
	{			
		for(int j = 0; j < this.img.width(); j++) {
			for (int i = 0; i < this.img.height(); i++) {
				double pixel = this.img.get(j, i)[0];
				if(pixel < value) {
					this.img.put(j, i, 0);
				}
				else {
					this.img.put(j, i, 255);
				}
					
			}
		}
	}
	
	public void TClamp(double value) 
	{	
		int num_bytes = (int)(this.img.total() * this.img.channels());
		byte[] tmp = new byte[num_bytes];
		this.img.get(0, 0, tmp);
		
		for(int j = 0; j < this.img.width(); j++) {
			for (int i = 0; i < this.img.height(); i++) {
				double pixel = this.img.get(j, i)[0];
				if(pixel < value) {
					this.img.put(j, i, 0);
				}
				else {
					this.img.put(j, i, 255);
				}
					
			}
		}
		
		this.img.put(0, 0, tmp);
		
	}
	
	
	
	public void ClampSTD(int num_std) {
		double value = this.GetAveragePixel();
		double stdev = this.GetStdDev();
		
		for(int j = 0; j < this.img.width(); j++) {
			for (int i = 0; i < this.img.height(); i++) {
				for (int k = 0; k < this.img.channels(); k++) {
					
					double pixel = this.img.get(j, i)[0];
					if(pixel < value + (stdev * num_std)) {
						this.img.put(j, i, 0);
					}
					else {
						this.img.put(j, i, 255);
					}
					
				}
			}
		}
	}
	
	public void ClampA() {
		double average_pixel = this.GetAveragePixel();
		System.out.println(average_pixel);
		System.out.println(this.GetStdDev());
		this.Clamp(average_pixel);
		return;
	}
	
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
	
	public void Invert() {
		Core.bitwise_not(this.img, this.img);
	}
	
	public int GetHeight() {
		return this.img.height();
	}
	
	public int GetWidth() {
		return this.img.width();
	}

	public Mat GetImg() {
		return this.img;
	}
	
}
