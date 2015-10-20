package com.github.exaper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CODE_TAKE_PHOTO = 0xAE80;
    private static final String EXTRA_IMAGE_URI = "image_uri";
    private ImageView mImage;
    private Uri mImageFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.takePictureButton).setOnClickListener(this);
        mImage = (ImageView) findViewById(R.id.image);
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_IMAGE_URI)) {
            mImageFileUri = savedInstanceState.getParcelable(EXTRA_IMAGE_URI);
            updateImage();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageFileUri != null) {
            outState.putParcelable(EXTRA_IMAGE_URI, mImageFileUri);
        }
    }

    @Override
    public void onClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                mImageFileUri = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                mImageFileUri = null;
            }

            // Continue only if the File was successfully created
            if (mImageFileUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageFileUri);
                startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO);
            }

        } else {
            Toast.makeText(this, "No camera here!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            switch (resultCode) {
                case RESULT_OK:
                    // Refresh with image we requested.
                    updateImage();
                    break;
                case RESULT_CANCELED:
                    mImageFileUri = null;
                    updateImage();
                    break;
                case RESULT_FIRST_USER:
                    // No-op something went wrong with camera.
                    break;
            }
        }
    }

    private void updateImage() {
        Glide.with(this)
                .load(mImageFileUri)
                .fitCenter()
                .into(mImage);
    }

    private Uri createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return Uri.parse("file:" + image.getAbsolutePath());
    }
}
