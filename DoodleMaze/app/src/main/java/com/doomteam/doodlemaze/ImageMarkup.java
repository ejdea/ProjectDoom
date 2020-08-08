package com.doomteam.doodlemaze;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Class used for android -> OpenCV operations and height map generation.
 *
 * @author Martin Edmunds
 * @since 2020-07-31
 * @version 1.3
 */
public class ImageMarkup extends android.app.Activity{

    public static int DEFAULT_RESOLUTION = 1025;
    private static int DEFAULT_DEPTH = 2;
    private static double CLAMP_STD_VALUE = -2;

    public Mat img;
    private byte[] height_map = null;

    /**
     * Class utility function to convert a list of integer positions into a byte array
     *
     * @param arr list of integers to be converted into bytes
     * @return byte representation of the integers
     * */
    public static Byte[] GetPositionBytes(List<Integer> arr){
        Byte[] to_return = new Byte[arr.size() * 4];
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
     * Constructor: Converts an existing Mat img into the a 1025x1025 1-channel greyscale image
     *
     * @throws CvException On missing file
     * */
    public ImageMarkup(Mat img) throws CvException {
        this.img = new Mat();
        img.copyTo(this.img);

        //convert image to gray scale
        ConvertTo1CGray();

        //resize image to 1025x1025 : resolution needed for height map
        Imgproc.resize(this.img,  this.img,  new Size(DEFAULT_RESOLUTION, DEFAULT_RESOLUTION));

    }

    /**
     * Constructor: Converts an existing Bitmap into the a 1025x1025 1-channel greyscale image
     *
     * @throws CvException On missing file
     * @throws NullPointerException On bitmap -> map conversion errors
     * */
    public ImageMarkup(Bitmap bitmap) throws CvException, NullPointerException{
        this.img = new Mat();

        //convert Android color bitmap to 1-channel grayscale
        Utils.bitmapToMat(bitmap, this.img);
        if(this.img == null){
            throw new NullPointerException("Unable to convert bitmap to mat!");
        }
        ConvertTo1CGray();
        if(this.img == null){
            throw new NullPointerException("Unable to convert bitmap to mat!");
        }
        Resize(DEFAULT_RESOLUTION, DEFAULT_RESOLUTION);
    }

    /**
     * Constructor: Converts an existing Bitmap into the a (x, y) 1-channel greyscale image
     *
     * @throws CvException On missing file
     * @throws NullPointerException On bitmap -> map conversion errors
     * */
    public ImageMarkup(Bitmap bitmap, int x, int y) throws CvException, NullPointerException{
        this.img = new Mat();

        //convert Android color bitmap to 1-channel grayscale
        Utils.bitmapToMat(bitmap, this.img);
        if(this.img == null){
            throw new NullPointerException("Unable to convert bitmap to mat!");
        }
        ConvertTo1CGray();
        if(this.img == null){
            throw new NullPointerException("Unable to convert bitmap to mat!");
        }
        Resize(x, y);
    }

    /**
     * Constructor: Converts an existing Bitmap into the a (x, y) 1-channel greyscale image. Uses static reference
     * to save memory
     *
     * @throws CvException On missing file
     * @throws NullPointerException On bitmap -> map conversion errors
     * */
    public ImageMarkup(int x, int y) throws CvException, NullPointerException{
        this.img = new Mat();

        //convert Android color bitmap to 1-channel grayscale
        Utils.bitmapToMat(OCRProcessor.croppedBmp, this.img);
        if(this.img == null){
            throw new NullPointerException("Unable to convert bitmap to mat!");
        }
        ConvertTo1CGray();
        if(this.img == null){
            throw new NullPointerException("Unable to convert bitmap to mat!");
        }
        Resize(x, y);
    }

    /**
     * Converts the current mat image to a 1-channel grayscale image
     *
     * @throws CvException on invalid conversion
     * @throws NullPointerException on invalid conversion
     * */
    public void ConvertTo1CGray() throws CvException, NullPointerException{
        Imgproc.cvtColor(this.img, this.img, Imgproc.COLOR_RGB2GRAY);
        this.img.convertTo(this.img, CvType.CV_8UC1);
    }

    /**
     * Resize the current img
     *
     * @param x target width of the image
     * @param y target height of the image
     * */
    public void Resize(int x, int y){
        Imgproc.resize(this.img, this.img,  new Size(x, y));
    }

    /**
     * Constructs the heightmap of the image. Format of the heightmap is a 16-bit 1-channel raw file.
     *
     * 1025x1025
     *
     * @throws CvException on resize error
     * @throws IndexOutOfBoundsException on byte copy error
     * */
    public void GenerateHeightMap() throws CvException, IndexOutOfBoundsException{
        this.GenerateHeightMap(1025, 1025);
    }

    /**
     * Constructs the heightmap of the image. Format of the heightmap is a 16-bit 1-channel raw file.
     *
     *
     * @param x width of the heightmap (must be between 0 and img.width)
     * @param y width of the heightmap (must be between 0 and img.height)
     *
     * @throws CvException on resize error
     * @throws IndexOutOfBoundsException on byte copy error
     * */
    public void GenerateHeightMap(int x, int y) throws CvException, IndexOutOfBoundsException{

        // 0 <= x, y <= this.img.dim()
        if(x <= 0 || y <= 0){
            x = DEFAULT_RESOLUTION;
            y = DEFAULT_RESOLUTION;
        }
        if(x > this.img.width() || y > this.img.height()){
            x = this.img.width();
            y = this.img.height();
        }

        //create copy of current Mat
        Mat src = new Mat();
        this.img.copyTo(src);

        //convert the image to x, y if not already done
        if(src.width() != x || src.height() != y){
            Imgproc.resize(src, src, new Size(x, y));
        }

        //create tmp copy array for fast pixel access
        byte[] tmp = new byte[src.width() * src.height()];
        byte[] height_map = new byte[src.width() * src.height() * DEFAULT_DEPTH];


        src.get(0, 0, tmp);

        int byte_index = 0;
        for(int i = 0; i < tmp.length; i++){
            int value = (byte)tmp[i] & 0xFF;
            height_map[byte_index] = 0x00;
            if(value == 255){
                height_map[byte_index + 1] = 0x00;
            }
            else{
                height_map[byte_index + 1] = 0x02;
            }
            byte_index += DEFAULT_DEPTH;
        }

        this.height_map = height_map;
    }

    /**
     * Writes the currently generated heightmap to a file location based on the context which called this
     * context_dir/app_height_maps/filename
     *
     * Example: com.doomteam.imageedit/files/app_height_maps/file.raw
     *
     * @param context parent context
     * @param filename name of the file (*.raw)
     *
     * @throws NullPointerException if the heightmap hasn't been generated
     * @throws IOException on file write error
     */
    public void WriteHeightMap(Context context, String filename) throws NullPointerException, IOException{
        if(this.height_map == null){
            throw new NullPointerException("Heightmap needs to be generated first!");
        }

        File dir = new File(context.getFilesDir(), "app_height_maps");
        if(!dir.exists()){
            dir.mkdir();
        }
        File out = new File(dir, filename);
        OutputStream outputStream = new FileOutputStream(out);
        outputStream.write(this.height_map);
        outputStream.flush();
        outputStream.close();
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
            this.ClampSTD(CLAMP_STD_VALUE);
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
                double pixel_value = (byte)(tmp[(j*height) + i]) & 0xFF;
                values[(j * height) + i] = (pixel_value - average) * (pixel_value - average);
                pre_total += values[(j * height) + i];
            }
        }

        return Math.sqrt(((double)(1.0 / num_elements) * pre_total));
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
                double pixel = (byte)((tmp[(j*height) + i])) & 0xFF;
                if(pixel < value) {
                    tmp[(j * height) + i] = 0;
                }
                else {
                    tmp[(j * height) + i] = (byte)255;
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
    public void ClampSTD(double num_std) {
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

    /**
     * Returns 1025x1025x16bit height map
     *
     *
     * @return raw byte array height map
     * */
    public byte[] GetHeightMap() {
        return this.height_map;
    }

    /**
     * Returns 1025x1025 Android bitmap
     *
     * @throws CvException on invalid conversion
     * @throws NullPointerException on invalid conversion
     * */
    public Bitmap Get1025Bitmap() throws CvException, NullPointerException{
        Bitmap map = Bitmap.createBitmap(DEFAULT_RESOLUTION, DEFAULT_RESOLUTION, Bitmap.Config.ARGB_8888);
        if(this.img.cols() == DEFAULT_RESOLUTION && this.img.rows() == DEFAULT_RESOLUTION) {
            //no resizing needed
            Utils.matToBitmap(this.img, map);
        }
        else{
            //Create 1025 mat from existing mat
            Mat src = new Mat();
            this.img.copyTo(src);

            //convert the image to x, y if not already done
            Imgproc.resize(src, src, new Size(DEFAULT_RESOLUTION, DEFAULT_RESOLUTION));
            Utils.matToBitmap(src, map);
        }
        return map;
    }

    /**
     * Removes pixel information from a bounding box specified by the arguments
     *
     *
     * @param x1 left box position
     * @param y1 top box position
     * @param x2 right box position
     * @param y2 bottom box position
     * @param offset_x pixel offset x-dim to start pixel removal
     * @param offset_y pixel offset y-dim to start pixel removal
     * */
    public void RemoveBoundingBox(int x1, int y1, int x2, int y2, int offset_x, int offset_y){
        int min_x = Math.min(x1, x2); int min_y = Math.min(y1, y2);
        int max_x = Math.max(x1, x2); int max_y = Math.max(y1, y2);

        // can't set an offset more than the midpoint of the box
        if((((max_x + min_x) / 2) < offset_x) || (((max_y + min_y) / 2) < 2*offset_y))
        {
            return;
        }

        // can't set an offset less than the minimum values of the bitmap
        if(min_y - offset_y < 0 || min_x - offset_x < 0)
        {
            return;
        }

        int width = this.img.width();
        int height = this.img.height();

        //copy image to temp byte array for faster processing
        int num_bytes = (int)(this.img.total() * this.img.channels());
        byte[] tmp = new byte[num_bytes];
        this.img.get(0, 0, tmp);

        for(int j = min_y + offset_y; j < max_y - offset_y; j++) {
            for (int i = min_x + offset_x; i < max_x - offset_x; i++) {
                tmp[(j * height) + i] = (byte)255;
            }
        }

        this.img.put(0, 0, tmp);
    }

    /**
     * Returns Android bitmap
     *
     * @throws CvException on invalid conversion
     * @throws NullPointerException on invalid conversion
     * */
    public Bitmap GetBitmap() throws CvException, NullPointerException{
        Bitmap map = null;
        map = Bitmap.createBitmap(this.img.cols(), this.img.rows(), Bitmap.Config.ARGB_8888);
        if(map == null){
            throw new NullPointerException();
        }
        Utils.matToBitmap(this.img, map);
        return map;
    }
}
