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

public class OCRProcessor {

    private static int NUM_OCR_ATTEMPTS = 10;
    private static int NUM_FILTER_ITERATIONS = 2;
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
        if(originalImage.getWidth() > 4000 || originalImage.getHeight() > 4000)
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

    private boolean StartDetectionRoutine()
    {
        if(croppedBmp == null || ocrImage == null)
        {
            return false;
        }

        int attempts = 0;
        Point startBoxTopLeft = null; Point startBoxBottomRight = null;
        Point endBoxTopLeft = null; Point endBoxBottomRight = null;

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
                            if(startBoxTopLeft == null && (elementText.equals("X") || elementText.equals("x"))) {
                                startBoxTopLeft = elementCornerPoints[0];
                                startBoxBottomRight = elementCornerPoints[2];
                            }
                            //retrieve bounding rect dimensions for 'o'
                            else if(endBoxTopLeft == null && (elementText.equals("O") || elementText.equals("o"))) {
                                endBoxTopLeft = elementCornerPoints[0];
                                endBoxBottomRight = elementCornerPoints[2];
                            }
                        }
                    }
                }
            }
            // check if detection routine is done
            if(startBoxTopLeft != null && startBoxBottomRight != null && endBoxTopLeft != null && endBoxBottomRight != null)
            {
                Log.d(TAG_INFO, "All characters were recognized");
                originalImage.recycle();

                // Generate ocvImage with all features recognized
                cleanImage(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION);
                positionData = removeOCRText(startBoxTopLeft, startBoxBottomRight, endBoxTopLeft, endBoxBottomRight, croppedBmp.getWidth(), croppedBmp.getHeight());
                croppedBmp.recycle();
                return true;
            }
            else{
                //hint for garbage collection to avoid heap exhaustion
                croppedBmp.recycle();

                //increase size of cropped by 1%
                cropOriginal(0.01);

                ocrImage = InputImage.fromBitmap(croppedBmp, 0);
            }
            attempts++;
        }
        return false;
    }

    /**
     * Runs google's Text Detection ML kit for Firebase on the selected picture
     *
     */
    private Task<Text> detectText() {
        TextRecognizer recognizer = TextRecognition.getClient();
        return recognizer.process(this.ocrImage).addOnSuccessListener(text -> {
            Log.d(TAG_INFO, "Text recognition succeeded");
        }).addOnFailureListener(e -> {
           Log.d(TAG_INFO, "Text recognition failed!");
        });
    }

    private void cleanImage(int width, int height) {
        if (croppedBmp == null) {
            Log.d(TAG_INFO, "WARNING: imageBitmap == null");
            return;
        }
        ocvImage = new ImageMarkup(croppedBmp, croppedBmp.getWidth(), croppedBmp.getHeight());
        ocvImage.Filter(NUM_FILTER_ITERATIONS);
    }

    /**
     * Function that removes the bounding boxes detected in the CV text detection method
     *
     *
     * @param startBoxTopLeft top left 'X'
     * @param startBoxBottomRight bottom right 'X'
     * @param endBoxTopLeft top left 'O'
     * @param endBoxBottomRight bottom right 'O'
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
            Log.d(TAG_INFO, "Warning: currentImage needs to be created before removing bounding boxes!");
            return null;
        }

        // remove the content in the bounding boxes mark
        ocvImage.Resize(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION);
        ocvImage.RemoveBoundingBox(sTLx, sTLy, sBRx, sBRy, -10, -10);
        ocvImage.RemoveBoundingBox(eTLx, eTLy, eBRx, eBRy, -10, -10);

        ocvImage.GenerateHeightMap();

        return new ArrayList<>(Arrays.asList(sTLx, sTLy, sBRx, sBRy, eTLx, eTLy, eBRx, eBRy));

    }

    /**
     * This program is used to modify the original image in an attempt for a better
     * OCR result
     *
     */
    private void cropOriginal(double amount){
        //increase cropped size by amount%
        int width = cx2Dim - cx1Dim;
        int height = cy2Dim - cy1Dim;
        int widthResize = (int)(amount * width);
        int heightResize = (int)(amount * height);

        //update new cropped image dimensions
        cx1Dim = Math.max(0, (int)(cx1Dim - widthResize));
        cx2Dim = Math.min(originalImage.getWidth(), (int)(cx2Dim + widthResize));
        cy1Dim = Math.max(0, (int)(cy1Dim - heightResize));
        cy2Dim = Math.min(originalImage.getHeight(), (int)(cy2Dim + heightResize));

        //create new cropped image
        croppedBmp.recycle();
        croppedBmp = Bitmap.createBitmap(originalImage, cx1Dim, cy1Dim, (cx2Dim - cx1Dim), (cy2Dim - cy1Dim));
    }

}
