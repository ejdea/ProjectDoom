package com.doomteam.doodlemaze;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
 * @version 1.1
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

    // Original crop dimensions
    int ocx1Dim;
    int ocx2Dim;
    int ocy1Dim;
    int ocy2Dim;

    // Absolute bounding box dimensions
    Point absXTL;
    Point absXBR;
    Point absOTL;
    Point absOBR;

    // Current cropped bounding box dimensions
    Point cXTL;
    Point cXBR;
    Point cOTL;
    Point cOBR;

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
        //clamp cropped coords
        x1Dim = clamp(x1Dim, originalImage.getWidth());
        x2Dim = clamp(x2Dim, originalImage.getWidth());
        y1Dim = clamp(y1Dim, originalImage.getHeight());
        y2Dim = clamp(y2Dim, originalImage.getHeight());

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
        ocx1Dim = cx1Dim *= scaleFactorX;
        ocx2Dim = cx2Dim *= scaleFactorX;
        ocy1Dim = cy1Dim *= scaleFactorY;
        ocy2Dim = cy2Dim *= scaleFactorY;

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
     * Successful attempts are able to locate an 'x' or an 'o'
     * On failure, attempts to reisze and try again
     *
     * Algorithm Works as Follows:
     *  while both characters haven't been found and tries < MAX_TRIES:
     *      if a character is detected:
     *          absolute position in relation to original image is recorded
     *          character is remove from original image
     *      if both characters haven't been found:
     *          resize the original cropped image space by 1%
     *  Transform the absolute coordinates of the objects to the original cropped image space
     *  Check that the objects coordinates are withing the original bounds selected by the user
     *  Generate the Heightmap
     *  Record the position of the characters in heightmap space (0, 0, 1025, 1025)
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
            if((text.contains("X") || text.contains("x")) || (text.contains("O") || text.contains("o"))) {
                for (Text.TextBlock block : result.getResult().getTextBlocks()) {
                    for (Text.Line line : block.getLines()) {
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();

                            //retrieve bounding rect dimensions for 'x'
                            if(!foundX && (elementText.contains("X") || elementText.contains("x"))) {
                                startBoxTopLeft = elementCornerPoints[0];
                                startBoxBottomRight = elementCornerPoints[2];
                                absXTL = new Point(startBoxTopLeft.x + Math.min(cx1Dim, cx2Dim), startBoxTopLeft.y + Math.min(cy1Dim, cy2Dim));
                                absXBR = new Point(startBoxBottomRight.x + Math.min(cx1Dim, cx2Dim), startBoxBottomRight.y + Math.min(cy1Dim, cy2Dim));

                                // character found, remove from original image using absolute coords
                                removeBoundingBoxBmp(absXTL, absXBR);

                                foundX = true;
                            }
                            //retrieve bounding rect dimensions for 'o'
                            else if(!foundO && (elementText.contains("O") || elementText.contains("o"))) {
                                endBoxTopLeft = elementCornerPoints[0];
                                endBoxBottomRight = elementCornerPoints[2];
                                absOTL = new Point(endBoxTopLeft.x + cx1Dim, endBoxTopLeft.y + cy1Dim);
                                absOBR = new Point(endBoxBottomRight.x + cx1Dim, endBoxBottomRight.y + cy1Dim);

                                // character found, remove from original image using absolute coords
                                removeBoundingBoxBmp(absOTL, absOBR);

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

                transformAbsCoordsToCroppedCoords();

                //ensure the identified objects are within bounds
                if(objectsInCroppedBounds()) {
                    cleanImage(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION);
                    positionData = getPositionData();
                    return true;
                }
                else{
                    //objects weren't correctly selected during cropping
                    return false;
                }
            }
            else{

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
     * Function that checks to see if objects are located in the original cropped bounds
     * If they aren't the user didn't correctly crop the characters
     *
     *
     * @return
     */
    private boolean objectsInCroppedBounds()
    {
        int min_x = Math.min(ocx1Dim, ocx2Dim);
        int max_x = Math.max(ocx1Dim, ocx2Dim);
        int min_y = Math.min(ocy1Dim, ocy2Dim);
        int max_y = Math.max(ocy1Dim, ocy2Dim);
        //original cropped bounds:
        if(isPointInBounds(max_x, min_x, max_y, min_y, absOTL) && isPointInBounds(max_x, min_x, max_y, min_y, absOBR) &&
                isPointInBounds(max_x, min_x, max_y, min_y, absXTL) && isPointInBounds(max_x, min_x, max_y, min_y, absXBR) )
        {
            return true;
        }
        return false;
    }

    /**
     * Helper function to check if a point is in a designated bounds
     *
     * @param max_x upper bound x
     * @param min_x lower bound x
     * @param max_y upper bound y
     * @param min_y lower bound y
     * @param p point to check
     * @return in bounds status
     */
    private boolean isPointInBounds(int max_x, int min_x, int max_y, int min_y, Point p)
    {
        return (p.x <= max_x && p.x >= min_x) && (p.y >= min_y && p.y <= max_y);
    }


    /**
     * Removes the bounding box detected from the original image
     *
     * @param p1 upper left bounding box coord
     * @param p2 bottom right bounding box coord
     */
    private void removeBoundingBoxBmp(Point p1, Point p2)
    {
        originalImage = originalImage.copy(originalImage.getConfig(), true);
        Canvas c = new Canvas(originalImage);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.rgb(255, 255, 255));

        int max_x = Math.max(p1.x, p2.x);
        int min_x = Math.min(p1.x, p2.x);
        int max_y = Math.max(p1.y, p2.y);
        int min_y = Math.min(p1.y, p2.y);
        // clamp to ensure not drawing out of bounds
        max_x = clamp(max_x, originalImage.getWidth());
        min_x = clamp(min_x, originalImage.getWidth());
        max_y = clamp(max_y, originalImage.getHeight());
        min_y = clamp(min_y, originalImage.getHeight());

        c.drawRect(new Rect(min_x, min_y, max_x, max_y), p);
    }

    /**
     * Clamps a value between 0 and value inclusive
     *
     *
     * @param p value to be clamped
     * @param value upper inclusive bounds
     * @return clamped value
     */
    private int clamp(int p, int value)
    {
        return Math.min(Math.max(0, p), value);
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

        ocvImage = new ImageMarkup(croppedBmp, width, height);
        ocvImage.Filter(NUM_FILTER_ITERATIONS);
    }

    /**
     * Transforms all absolute coordinates for the bounding box to original croppedImage space:
     * (0, 0, croppedWidth, croppedHeight)
     *
     *
     */
    private void transformAbsCoordsToCroppedCoords()
    {
        // create new cropped BMP with user's original settings
        croppedBmp = Bitmap.createBitmap(originalImage, ocx1Dim, ocy1Dim, ocx2Dim - ocx1Dim, ocy2Dim - ocy1Dim);

        //calculate new points for bounding box in relation to new cropped image
        cXTL = transformPointOriginalToCropped(absXTL);
        cXBR = transformPointOriginalToCropped(absXBR);
        cOTL = transformPointOriginalToCropped(absOTL);
        cOBR = transformPointOriginalToCropped(absOBR);
    }

    /**
     * Transforms a point from the original image to the cropped location
     *
     * cropped point = absolute point - cropped offset
     *
     * @param p1 point to be transformed
     * @return p transformed coordinate in (0, 0, croppedWidth, croppedHeight) space
     */
    private Point transformPointOriginalToCropped(Point p1)
    {
        Point p = new Point();
        p.x = p1.x - Math.min(ocx1Dim, ocx2Dim);
        p.y = p1.y - Math.min(ocy1Dim, ocy2Dim);

        return p;
    }

    /**
     * Retrieves position data in heightmap space (1025x1025)
     * And generates the heightmap from the ocvImage
     *
     * @return position data in heightmap space (0, 0, 1025, 1025)
     */
    private ArrayList<Integer> getPositionData()
    {
        int sTLx = (int)(((float)cXTL.x / croppedBmp.getWidth()) * (float)HEIGHTMAP_RESOLUTION);
        int sTLy = (int)(((float)cXTL.y / croppedBmp.getHeight()) * (float)HEIGHTMAP_RESOLUTION);
        int sBRx = (int)(((float)cXBR.x / croppedBmp.getWidth()) * (float)HEIGHTMAP_RESOLUTION);
        int sBRy = (int)(((float)cXBR.y / croppedBmp.getHeight()) * (float)HEIGHTMAP_RESOLUTION);

        int eTLx = (int)(((float)cOTL.x / croppedBmp.getWidth()) * (float)HEIGHTMAP_RESOLUTION);
        int eTLy = (int)(((float)cOTL.y / croppedBmp.getHeight()) * (float)HEIGHTMAP_RESOLUTION);
        int eBRx = (int)(((float)cOBR.x / croppedBmp.getWidth()) * (float)HEIGHTMAP_RESOLUTION);
        int eBRy = (int)(((float)cOBR.y / croppedBmp.getHeight()) * (float)HEIGHTMAP_RESOLUTION);

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
