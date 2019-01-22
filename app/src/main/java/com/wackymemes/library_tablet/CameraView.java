package com.wackymemes.library_tablet;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.wackymemes.library_tablet.CurrentUser;
import com.wackymemes.library_tablet.FaceDetector;
import com.wackymemes.library_tablet.NaamatauluAPI;
import com.wackymemes.library_tablet.UploadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraView.class.getSimpleName();
    private SurfaceHolder holder;
    private Camera camera;
    private Context context;
    private FaceDetector faceDetector;

    public enum State {
        NO_USER,
        RECOGNIZING,
        USING,
        LOGOUT
    }

    State state = State.NO_USER;

    private Runnable clearUser = new Runnable() {
        @Override
        public void run() {
            CurrentUser.getInstance().clearUser();
            state = State.NO_USER;
            Toast.makeText(context, "User logged out", Toast.LENGTH_LONG).show();
        }
    };

    private Camera.FaceDetectionListener fdListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                Log.d(TAG, faces.length + " face(s)");
                switch (state) {
                    case NO_USER:
                        state = State.RECOGNIZING;
                        camera.takePicture(null, null, null, pictureCallback);
                        Toast.makeText(context, "Recognizing...", Toast.LENGTH_SHORT).show();
                        break;
                    case RECOGNIZING:
                        for (Camera.Face face : faces) {
                            Log.d(TAG, String.valueOf(face.rect.height()));
                            if (face.rect.height() > 400) {
                                CurrentUser.getInstance().setLoggedIn();
                                state = State.USING;
                                Toast.makeText(context, "Hello, " + CurrentUser.getInstance().getUsername(), Toast.LENGTH_LONG).show();
                                break;
                            }
                        }
                        break;
                    case USING:
                        for (Camera.Face face : faces) {
                            if (face.rect.height() > 400) {
                                break;
                            }
                            state = State.LOGOUT;
                            Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show();
                            getHandler().postDelayed(clearUser, 3000);
                        }
                        break;
                    case LOGOUT:
                        getHandler().removeCallbacks(clearUser);
                        Toast.makeText(context, "Back again", Toast.LENGTH_SHORT).show();
                        state = State.USING;
                }
            }
            if (faces.length == 0) {
                Log.d(TAG, "No faces");
                switch (state) {
                    // TODO: pohdipa missä vaiheessa tyhjätä käyttäjä, tuleeko tää liian aikaisin?
                    case RECOGNIZING:
                        getHandler().post(clearUser);
                        break;
                    case USING:
                        state = State.LOGOUT;
                        Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show();
                        getHandler().postDelayed(clearUser, 3000);
                        break;
                }
            }
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, final Camera camera) {
            Log.d(TAG, "onPictureTaken");
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    File photo = pictureTaken(data);
                    File cropped = faceDetector.cropLargestFace(photo.getAbsolutePath());
                    if (cropped == null) {
                        Log.e(TAG, "Cannot get image");
                        state = State.NO_USER;
                        return;
                    }
                    sendCropped(cropped);
                }
            });
        }
    };


    public CameraView(Context context) { this(context, null); }

    public CameraView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    // TODO: luodaan uusi cameraview activityn vaihtuessa -> täytyy tarkistaa currentuserilta onko joku aktiivisena
    public CameraView(Context context, AttributeSet attrs, int defStyleattr) {
        super(context, attrs, defStyleattr);
        holder = getHolder();
        holder.addCallback(this);
        this.context = context;
        faceDetector = new FaceDetector(context, (Activity)context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        openCamera();
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.startFaceDetection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void openCamera() {
        if (camera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int cameraId = 0;
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                }
            }
            camera = Camera.open(cameraId);
            camera.setFaceDetectionListener(fdListener);
        }
    }

    private File pictureTaken(byte[] data) {
        File photo = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg");
        OutputStream os = null;
        try {
            os = new FileOutputStream(photo);
            os.write(data);
            os.close();
        } catch (IOException e) {
            state = State.NO_USER;
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
        camera.startPreview();
        return photo;
    }

    private void sendCropped(File cropped) {
        new NaamatauluAPI(context, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                if (result == null) {
                    Log.d(TAG, "Not identified");
                    Toast.makeText(context, "Not identified", Toast.LENGTH_LONG).show();
                    state = State.NO_USER;
                    return;
                }
                if (CurrentUser.getInstance().processJson(result)) {
                    Toast.makeText(context, "Recognized", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Didn't recognize you", Toast.LENGTH_SHORT).show();
                    state = State.NO_USER;
                }
                camera.startFaceDetection();
            }
        }).execute(cropped);
    }
}
