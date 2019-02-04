package com.wackymemes.library_tablet;

import android.app.Activity;
import android.content.Context;
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
    private int userId;

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
                i.putExtra("id", userId);
                i.putExtra("action", "reserve");
                startActivity(i);
                finish();
            }
        });

        Button returnButton = (Button) findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(PersonalActivity.this, BarcodeScannerActivity.class);
                i.putExtra("id", userId);
                i.putExtra("action", "return");
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
        userId = getIntent().getExtras().getInt("id");
        TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
        helloTextView.setText("Hello, " + username);

        Button bookView1 = findViewById(R.id.button7);
        Button bookView2 = findViewById(R.id.button11);
        Button bookView3 = findViewById(R.id.button12);
        Button bookView4 = findViewById(R.id.button13);
        Button[] bookViewArr = {bookView1, bookView2, bookView3, bookView4};
        LibraryAPI personalView = new LibraryAPI(this);
        personalView.getLoans(userId, bookViewArr);
    }
}
