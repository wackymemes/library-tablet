package com.wackymemes.library_tablet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wackymemes.library_tablet.FaceDetector;
import com.wackymemes.library_tablet.NaamatauluAPI;
import com.wackymemes.library_tablet.UploadListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "CameraActivity";
    Camera camera;
    SurfaceHolder holder;

    private final AtomicBoolean recognizing = new AtomicBoolean(false);
    private FaceDetector faceDetector = new FaceDetector(this, (Activity)this);
    private Handler handler;

    private Camera.Face[] lastFaces;

    File cropLargestFace(String fileName, Camera.Face[] faces) {
        double largestSize = 0;
        Camera.Face largestFace = null;
        for (int i = 0; i < faces.length; i++) {
            double faceSize = faces[i].rect.height(); // They are rectangles so it doesn't matter which dimension we use
            if (faceSize > largestSize) {
                largestSize = faceSize;
                largestFace = faces[i];
            }
        }
        Bitmap bMap = BitmapFactory.decodeFile(fileName);

        Bitmap croppedBmp = Bitmap.createBitmap(bMap, -largestFace.rect.left, -largestFace.rect.top, largestFace.rect.width(), largestFace.rect.height());

        File file = new File(fileName);

        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            croppedBmp.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return file;
    }

    private Camera.FaceDetectionListener fdListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                lastFaces = faces;
                Log.d(TAG, faces.length + " face(s)");
                if (!recognizing.getAndSet(true)) {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPictureSize(1280, 720);
                    camera.setParameters(parameters);
                    camera.takePicture(null, null, null, pictureCallback);
                }
            }
            if (faces.length == 0) {
                // TODO: to be used when user leaves the device and will be logged out
            }
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    File photo = pictureTaken(data);
                    File cropped = faceDetector.cropLargestFace(photo.getAbsolutePath());
                    //File cropped = cropLargestFace(photo.getAbsolutePath(), lastFaces);
                    if (cropped == null) {
                        Toast.makeText(CameraActivity.this.getApplicationContext(), "User not recognized", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Cannot get image");
                        startMainActivity();
                        return;
                    }
                    sendCropped(cropped);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SurfaceView surface = (SurfaceView)findViewById(R.id.cameraPreview);
        holder = surface.getHolder();
        holder.addCallback(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        holder.setFixedSize(height, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            openCamera();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.startFaceDetection();
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e);
        }
    }

    private void openCamera() {
        if (camera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int defaultCameraId = 0;
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    defaultCameraId = i;
                }
            }
            camera = Camera.open(defaultCameraId);
            camera.setFaceDetectionListener(fdListener);
        }
    }

    private Handler getHandler() {
        if (handler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            handler = new Handler(thread.getLooper());
        }
        return handler;
    }

    private File pictureTaken(byte[] data) {
        File photo = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg");
        OutputStream os = null;
        try {
            os = new FileOutputStream(photo);
            os.write(data);
            os.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot write to " + e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return photo;
    }

    private void startMainActivity () {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }

    private void startPersonalActivity (String username) {
        Intent i = new Intent(this, PersonalActivity.class);
        i.putExtra("user", username);
        startActivity(i);
    }

    private void sendCropped(File cropped) {
        new NaamatauluAPI(this, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                if (result == null) {
                    Toast.makeText(CameraActivity.this.getApplicationContext(), "User not recognized", Toast.LENGTH_LONG).show();
                    startMainActivity();
                    return;
                }

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
                startPersonalActivity(myMap.get(0).get("username"));
            }
        }).execute(cropped);
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
    }
}
