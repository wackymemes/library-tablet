package com.wackymemes.library_tablet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import butterknife.ButterKnife;

import static org.opencv.android.CameraRenderer.LOGTAG;

public class PersonalActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String photoPath;
    private boolean photoTaken = false;
    private String photoS;
    private String username;

    private FaceDetector faceDetector = new FaceDetector(this, (Activity)this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            photoPath = savedInstanceState.getString("photo_path");
            photoTaken = savedInstanceState.getBoolean("photo_taken");
        }
        setContentView(R.layout.activity_personal);
        ButterKnife.bind(this);
        if (!photoTaken) {
            dispatchTakePictureIntent();
        }

        Button reserveButton = (Button) findViewById(R.id.reserveButton);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(PersonalActivity.this, BarcodeScannerActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (photoPath != null) {
            outState.putString("photo_path", photoPath);
        }
        outState.putBoolean("photo_taken", photoTaken);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(LOGTAG, "Starting...");
            //faceDetector.transferImgFromDownloadToInternal(photoPath, "face.png");
            File img = faceDetector.cropLargestFace(photoPath);
            if (img == null && false) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            }

            new NaamatauluAPI(new UploadListener() {
                @Override
                public void onUploadCompleted(String result) {
                    if (result == null)
                        return;

                    List<Map<String, String>> myMap = new ArrayList<>();
                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        myMap = mapper.readValue(result, (new ArrayList<>()).getClass());
                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    username = myMap.get(0).get("username");
                    TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
                    helloTextView.setText("Hello, " + username);
                }
            }).execute(img);
        } else {
            TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
            helloTextView.setText("User not recognized");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String authorities = getApplicationContext().getPackageName() + ".fileprovider";
            if (photoFile != null) {
                Uri photoURI = null;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    photoURI = Uri.fromFile(photoFile);
                } else {

                    photoURI = FileProvider.getUriForFile(this, authorities, photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "FREC_" + timeStamp;
        File storageDir = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        photoPath = image.getAbsolutePath();
        return image;
    }

}
