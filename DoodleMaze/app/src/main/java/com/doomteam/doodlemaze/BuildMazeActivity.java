package com.doomteam.doodlemaze;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.Objects;

public class BuildMazeActivity extends Activity implements OnTouchListener {

    private static final String  TAG              = "MainActivity";

    private Mat mat_img;
    private ImageView imageView;
    private Bitmap bitmap_img;

    /*
     * Load OpenCV before class is instantiated
     * */
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        else{
            Log.d(TAG, "OpenCV loaded successfully");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Photo has been selected");

        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        //Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        this.bitmap_img = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                this.bitmap_img = BitmapFactory.decodeFile(picturePath);
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }

        Log.d(TAG, "Photo has been loaded");

        GenerateHeightMap();
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take New Photo", "Choose from Gallery", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose a maze image");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            //build intent objects based on what the user selected
            @Override
            public void onClick(DialogInterface dialog, int item) {

                //take a new photo, return an image
                if (options[item].equals("Take New Photo")) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } //choose a new photo, return
                else if (options[item].equals("Choose from Gallery")) {
                    //change directory
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //Log.d(TAG, "OpenCV loaded successfully");
                    //this.img = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public BuildMazeActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.test_main);

        this.selectImage(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false; // don't need subsequent touch events right now
    }

    private void GenerateHeightMap(){
        ImageView image_container = findViewById(R.id.imageView1);
        ImageMarkup img = new ImageMarkup(this.bitmap_img, 1025, 1025);

        img.Filter(10);
        img.GenerateHeightMap();

        try{
            img.WriteHeightMap(this, "mobile_height_map.raw");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        image_container.setImageBitmap(img.GetBitmap());


        Log.d(TAG, "Bitmap converted to Mat");
    }

}