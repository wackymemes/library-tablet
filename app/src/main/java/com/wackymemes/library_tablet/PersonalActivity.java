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

        Button reserveButton = (Button) findViewById(R.id.reserveButton);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(PersonalActivity.this, BarcodeScannerActivity.class);
                startActivity(i);
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(PersonalActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        username = getIntent().getExtras().getString("user");
        TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
        helloTextView.setText("Hello, " + username);
    }
}
