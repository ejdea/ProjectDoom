package com.doomteam.doodlemaze;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

/**
 * Class that controls the OCR process and manages all necessary resources for the process
 * Most resources are made static to prevent copies of large Bitmap objects being passed around
 *
 * NUM_OCR_ATTEMPTS - Number of attempts to make before failing the OCR procedure
 * NUM_FILTER_ITERATIONS - Number of times to filter the image to remove noise
 * MAX_IMAGE_SIZE - Max bounds to work with for the cropped BMP
 *
 * originalImage - set before instantiating class
 * positionData - Game object positioning data (Rect starting_location, Rect ending_location)
 * good - status flag that depicts state of OCR result
 *
 * @author Martin Edmunds
 * @since 2020-08-08
 * @version 1.0
 */
public class OCRProcessor {

    private static int NUM_OCR_ATTEMPTS = 10;
    private static int NUM_FILTER_ITERATIONS = 5;
    private static int MAX_IMAGE_SIZE = 4000;
    private static int HEIGHTMAP_RESOLUTION = 1025;
    public static Bitmap originalImage = null;
    public static Bitmap croppedBmp = null;
    public static ImageMarkup ocvImage = null;
    public static ArrayList<Integer> positionData;
    public static boolean good = true;

    private String TAG_INFO = "OCRProcessor: ";
    private InputImage ocrImage;

    // Crop image dimensions
    int cx1Dim;
    int cx2Dim;
    int cy1Dim;
    int cy2Dim;

    /**
     * OCRProcessor Constructor: Performs the following
     *  1. Assumes that original image has been set with a call to OCRProcess.originalImage = some_bitmap
     *  2. If the original image is above 4000, 4000 resolution, rescales the image keeping aspect ratio.
     *  3. Creates a new lower resolution cropped image from the original bitmap.
     *  4. Creates an OCRImage instance to perform Google's OCR
     *  5. Runs the detection routine
     *      5a. If detection failes, upsizes the image by 1% and tries again
     *  6. If Recognition is successful, OCRProcess.good bit set to true.
     *
     * @param x1Dim Upper left bounding box coord from image cropping routine
     * @param x2Dim Lower right bounding box coord from image cropping routine
     * @param y1Dim Upper left bounding box coord from image cropping routine
     * @param y2Dim Lower right bounding box coord from image cropping routine
     */
    OCRProcessor(int x1Dim, int x2Dim, int y1Dim, int y2Dim)
    {
        if(originalImage == null)
        {
            good = false;
            return;
        }

        int oldWidth = originalImage.getWidth();
        int oldHeight = originalImage.getHeight();
        float scaleFactorY = 1.0f;
        float scaleFactorX = 1.0f;

        //determine if original image needs scaled down to save memory
        if(originalImage.getWidth() > MAX_IMAGE_SIZE || originalImage.getHeight() > MAX_IMAGE_SIZE)
        {
            float ratio = Math.min(
                    (float) MAX_IMAGE_SIZE / originalImage.getWidth(),
                    (float) MAX_IMAGE_SIZE / originalImage.getHeight());
            int width = Math.round((float) ratio * originalImage.getWidth());
            int height = Math.round((float) ratio * originalImage.getHeight());

            originalImage = Bitmap.createScaledBitmap(originalImage, width,
                    height, true);

            //determine how much scaling was needed
            scaleFactorX = (float)width / (float)oldWidth;
            scaleFactorY = (float)height / (float)oldHeight;
        }

        // set user cropped dimensions
        cx1Dim = x1Dim;
        cx2Dim = x2Dim;
        cy1Dim = y1Dim;
        cy2Dim = y2Dim;

        //update cropped dimensions
        cx1Dim *= scaleFactorX;
        cx2Dim *= scaleFactorX;
        cy1Dim *= scaleFactorY;
        cy2Dim *= scaleFactorY;

        // load cropped bmp
        croppedBmp = Bitmap.createBitmap(originalImage, cx1Dim, cy1Dim, (cx2Dim - cx1Dim), (cy2Dim - cy1Dim));

        // set OCR image
        ocrImage = InputImage.fromBitmap(croppedBmp, 0);

        if(croppedBmp != null)
        {
            if(StartDetectionRoutine()) {
                good = true;
            }
            else{
                good = false;
            }
        }
        else
        {
            good = false;
        }
    }

    /**
     * Runs the current OCR method (Google's Firebase OCR)
     * Successful attempts are able to locate an 'x' and an 'o'
     * On failure, attempts to reisze and try again
     *
     *
     * @return boolean OCR success status
     */
    private boolean StartDetectionRoutine()
    {
        if(croppedBmp == null || ocrImage == null)
        {
            return false;
        }

        int attempts = 0;
        Point startBoxTopLeft = null; Point startBoxBottomRight = null;
        Point endBoxTopLeft = null; Point endBoxBottomRight = null;

        boolean foundX = false;
        boolean foundO = false;

        while(attempts < NUM_OCR_ATTEMPTS){

            //run text detection
            Task<Text> result = detectText();

            //wait for task to complete
            while(!result.isComplete()) {}
            String text = result.getResult().getText();

            //check for recognition of X and O characters
            if((text.contains("X") || text.contains("x")) && (text.contains("O") || text.contains("o"))) {
                for (Text.TextBlock block : result.getResult().getTextBlocks()) {
                    for (Text.Line line : block.getLines()) {
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            //retrieve bounding rect dimensions for 'x'
                            if(startBoxTopLeft == null && (elementText.contains("X") || elementText.contains("x"))) {
                                startBoxTopLeft = elementCornerPoints[0];
                                startBoxBottomRight = elementCornerPoints[2];
                                foundX = true;
                            }
                            //retrieve bounding rect dimensions for 'o'
                            else if(endBoxTopLeft == null && (elementText.contains("O") || elementText.contains("o"))) {
                                endBoxTopLeft = elementCornerPoints[0];
                                endBoxBottomRight = elementCornerPoints[2];
                                foundO = true;
                            }
                        }
                    }
                }
            }
            // check if detection routine detected all the necessary characters
            if(foundX && foundO)
            {
                Log.d(TAG_INFO, "All characters were recognized");
                // Generate ocvImage with all features recognized
                cleanImage(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION);
                positionData = removeOCRText(startBoxTopLeft, startBoxBottomRight, endBoxTopLeft, endBoxBottomRight, croppedBmp.getWidth(), croppedBmp.getHeight());
                return true;
            }
            else{
                //hint for garbage collection to avoid heap exhaustion

                //increase size of cropped by 1%
                cropOriginal(0.01);

                ocrImage = InputImage.fromBitmap(croppedBmp, 0);
            }
            attempts++;
        }
        Log.d(TAG_INFO, "Characters weren't found in OCR process");
        return false;
    }

    /**
     * Runs google's Text Detection ML kit for Firebase on the selected picture
     *
     * @return Task currently running OCR task
     */
    private Task<Text> detectText() {
        TextRecognizer recognizer = TextRecognition.getClient();
        return recognizer.process(this.ocrImage).addOnSuccessListener(text -> {
            Log.d(TAG_INFO, "Text recognition succeeded");
        }).addOnFailureListener(e -> {
           Log.d(TAG_INFO, "Text recognition failed!");
        });
    }

    /**
     * Creates an ocvImage from the ImageMarkup class and performs image filtering on the image
     * Make sure that a usable cropped image is available for this process
     *
     * @throws NullPointerException on croppedBmp not being set
     * @param width width of the OpenCV Mat to be created
     * @param height height of the OpenCV Mat to be created
     */
    private void cleanImage(int width, int height) {
        if (croppedBmp == null){
            throw new NullPointerException();
        }
        ocvImage = new ImageMarkup(croppedBmp, croppedBmp.getWidth(), croppedBmp.getHeight());
        ocvImage.Filter(NUM_FILTER_ITERATIONS);
    }

    /**
     * Function that removes the bounding boxes detected in the CV text detection method
     *
     * @throws NullPointerException On ocvImage not being created successfully
     * @param startBoxTopLeft top left 'X'
     * @param startBoxBottomRight bottom right 'X'
     * @param endBoxTopLeft top left 'O'
     * @param endBoxBottomRight bottom right 'O'
     * @return ArrayList array consisting of location of game objects
     */
    private ArrayList<Integer> removeOCRText(Point startBoxTopLeft, Point startBoxBottomRight, Point endBoxTopLeft, Point endBoxBottomRight, int maxWidth, int maxHeight){
        //convert pixel locations to 1025x1025 space
        int sTLx = (int)(((float)(startBoxTopLeft.x) / (float)maxWidth) * (int)HEIGHTMAP_RESOLUTION);
        int sTLy =  (int)((((float)startBoxTopLeft.y) / (float)maxHeight) * (int)HEIGHTMAP_RESOLUTION);
        int sBRx = (int)((((float)startBoxBottomRight.x) / (float)maxWidth) * (int)HEIGHTMAP_RESOLUTION);
        int sBRy =  (int)((((float)startBoxBottomRight.y) / (float)maxHeight) * (int)HEIGHTMAP_RESOLUTION);

        int eTLx = (int)((((float)endBoxTopLeft.x) / (float)maxWidth) * (int)HEIGHTMAP_RESOLUTION);
        int eTLy =  (int)(((float)(endBoxTopLeft.y) / (float)maxHeight) * (int)HEIGHTMAP_RESOLUTION);
        int eBRx = (int)(((float)(endBoxBottomRight.x) / (float)maxWidth) * (int)HEIGHTMAP_RESOLUTION);
        int eBRy =  (int)(((float)(endBoxBottomRight.y) / (float)maxHeight) *(int) HEIGHTMAP_RESOLUTION);


        if(ocvImage == null)
        {
            throw new NullPointerException();
        }

        // remove the content in the bounding boxes mark
        ocvImage.Resize(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION);
        ocvImage.RemoveBoundingBox(sTLx, sTLy, sBRx, sBRy, -10, -10);
        ocvImage.RemoveBoundingBox(eTLx, eTLy, eBRx, eBRy, -10, -10);

        ocvImage.GenerateHeightMap();

        return new ArrayList<>(Arrays.asList(sTLx, sTLy, sBRx, sBRy, eTLx, eTLy, eBRx, eBRy));

    }

    /**
     * Method performs a crop on the original image
     *
     * @param amount % amount to change the image
     * @throws NullPointerException on original image not being set
     */
    private void cropOriginal(double amount){
        //increase cropped size by amount%
        int width = cx2Dim - cx1Dim;
        int height = cy2Dim - cy1Dim;
        int widthResize = (int)(amount * width);
        int heightResize = (int)(amount * height);

        if(originalImage == null)
        {
            throw new NullPointerException();
        }

        //update new cropped image dimensions
        cx1Dim = Math.max(0, (int)(cx1Dim - widthResize));
        cx2Dim = Math.min(originalImage.getWidth(), (int)(cx2Dim + widthResize));
        cy1Dim = Math.max(0, (int)(cy1Dim - heightResize));
        cy2Dim = Math.min(originalImage.getHeight(), (int)(cy2Dim + heightResize));

        //create new cropped image
        croppedBmp = Bitmap.createBitmap(originalImage, cx1Dim, cy1Dim, (cx2Dim - cx1Dim), (cy2Dim - cy1Dim));
    }

}
