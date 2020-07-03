package com.imgae.imageedit;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class ImageMarkup {

    public static int DEFAULT_RESOLUTION = 1025;
    private static int DEFAULT_DEPTH = 2;

    public Mat img;
    private byte[] height_map;

    /**
     * Constructor: attempts to load file for processing
     *
     * @param filename The image file to attempt to be loaded
     * @throws CvException On missing file
     * */
    public ImageMarkup(String filename) throws CvException {
        this.img = Imgcodecs.imread(filename, Imgcodecs.IMREAD_GRAYSCALE);
        //resize image to 1025x1025 : resolution needed for height map
        Imgproc.resize(this.img,  this.img,  new Size(DEFAULT_RESOLUTION, DEFAULT_RESOLUTION));
        this.height_map = new byte[this.img.width() * this.img.height() * DEFAULT_DEPTH];
    }

    /**
     * Constructor: attempts to load file for processing
     *
     * @throws CvException On missing file
     * */
    public ImageMarkup(Mat img) throws CvException {
        this.img = new Mat();
        img.copyTo(this.img);
        Imgproc.cvtColor(this.img, this.img, Imgproc.COLOR_RGB2GRAY);

        this.img.convertTo(this.img, CvType.CV_8UC1);
        //resize image to 1025x1025 : resolution needed for height map
        Imgproc.resize(this.img,  this.img,  new Size(DEFAULT_RESOLUTION, DEFAULT_RESOLUTION));
        this.height_map = new byte[this.img.width() * this.img.height() * DEFAULT_DEPTH];
    }

    /**
     * Constructs the heightmap of the image. Format of the heightmap is a 16-bit 1-channel raw file.
     *
     *
     *
     * */
    public void GenerateMap() throws IndexOutOfBoundsException{
        byte[] tmp = new byte[img.width() * img.height()];
        this.img.get(0, 0, tmp);

        int byte_index = 0;
        for(int i = 0; i < tmp.length; i++){
            int value = (byte)tmp[i] & 0xFF;
            this.height_map[byte_index] = 0x00;
            if(value == 255){
                this.height_map[byte_index + 1] = 0x00;
            }
            else{
                this.height_map[byte_index + 1] = 0x01;
            }
            byte_index += DEFAULT_DEPTH;
        }

        /* CHANGED FROM: NEED TO TEST
        for(int j = 0; j < img.getWidth(); j++) {
            for(int i = 0; i < img.getHeight(); i++) {

                // -1 = white = heightmap = 0x0100 0x01, 0x00
                // else = black = heightmap = 0x0000 0x00, 0x00
                this.height_map[byte_index] = 0x00;
                if(img.getRGB(j, i) == white_pixel_int) {
                    this.byte_buffer[byte_index + 1] = 0x00;
                }
                else {
                    this.height_map[byte_index + 1] = 0x01;
                }
                byte_index += depth;
            }
        }
        */
    }

    /**
     * Blurs the image using GaussianBlur with a (size_x, size_y) kernel
     *
     * @param size_x kernel size in x dim
     * @param size_y kernel size in y dim
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
     * Performs multiple 9x9 blurs and clamps within two standard deviations to reduce noise in image
     *
     * @param num_times number of times to perform filtering
     * */
    public void Filter(int num_times) {
        for(int i = 0; i < num_times; i++) {
            this.Blur(9, 9);
            this.ClampSTD(-2);
        }
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
        int width = this.img.width();
        int height = this.img.height();
        double average = this.GetAveragePixel();
        int num_elements = width * height;
        double pre_total = 0;

        //copy image to temp byte array for faster processing
        int num_bytes = (int)(this.img.total() * this.img.channels());
        byte[] tmp = new byte[num_bytes];
        this.img.get(0, 0, tmp);

        double values[] = new double[width * height];
        //Standard deviation formula
        for(int j = 0; j < width; j++) {
            for (int i = 0; i < height; i++) {
                double pixel_value = (byte)(tmp[(j*width) + i]) & 0xFF;
                values[(j * width) + i] = (pixel_value - average) * (pixel_value - average);
                pre_total += values[(j * width) + i];
            }
        }

        return Math.sqrt(((double)(1.0 / num_elements) * pre_total));
    }


    /*

    /**
     * Returns the current Mat image as a BufferedImage usable by the Java 2D module
     *
     * @return BufferedImage Mat converted to BufferedImage
     *
    public Bitmap GetBufferedImage() {

        BufferedImage bi_return = new BufferedImage(this.img.width(), this.img.height(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = ((DataBufferByte) bi_return.getRaster().getDataBuffer()).getData();
        this.img.get(0, 0, data);

		/*
		for(int j = 0; j < width; j++) {
			for (int i = 0; i < height; i++) {
				data[(j * width) + i] = (byte)(this.img.get(j, i)[0]);
			}
		}


        return bi_return;

    }


    */

    /**
     * Default Clamp call that clamps the image to within 1 standard deviation
     *
     *
     *
     * */
    public void ClampSTD() {
        this.ClampSTD(1);
    }

    /**
     * Clamps the pixel values in the image against a value:
     * Pixels less than the threshold are set to 0. Everything else is set to 255.
     *
     *
     * @param value the value to be clamped against
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
     * @param num_std number of standard deviations to clamp to
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
     *
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
     *
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
     *
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
