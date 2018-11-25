package com.wackymemes.library_tablet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.opencv.android.CameraRenderer.LOGTAG;
import static org.opencv.imgproc.Imgproc.rectangle;

public class FaceDetector {

    private Context context;
    private Activity activity;

    public FaceDetector(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        if (!OpenCVLoader.initDebug()) {
            Log.e(LOGTAG, "OpenCV library not found.");
        }
    }

    // TODO remove
    // This is a debug helper function to transfer files from Download to internal storage
    public void transferImgFromDownloadToInternal (String fileName, String destFileName) {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this.activity, permissions, 0);

        String file = fileName;
        File imgFile = new File(file);

        int size = (int) imgFile.length();
        System.out.println("size."+size);
        byte[] bytes = new byte[size];

        try {
            DataInputStream buf = new DataInputStream(new FileInputStream(imgFile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

        Imgcodecs.imwrite(context.getFilesDir().getPath() + "/" + destFileName, mat);
    }

    public File cropLargestFace (String fileName) {
        //transferImgFromDownloadToInternal(fileName, "face.png");
        String path = fileName;
        Mat img = Imgcodecs.imread(path);
        Log.d(LOGTAG, "IMG SIZE "+img.size());

        if (img.empty()) {
            Log.e(LOGTAG, "Reading image from " + path + " failed");
            return null;
        }

        MatOfRect faces = new MatOfRect();

        try {
            Mat grayscaleImage = new Mat(img.width(), img.height(), CvType.CV_8UC4);
            Imgproc.cvtColor(img, grayscaleImage, Imgproc.COLOR_RGBA2RGB);

            File classifierFile = new File(context.getCacheDir()+"/haarcascade_frontalface_alt.xml");
            if (!classifierFile.exists()) try {

                InputStream is = context.getAssets().open("haarcascade_frontalface_alt.xml");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();


                FileOutputStream fos = new FileOutputStream(classifierFile);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) { throw new RuntimeException(e); }

            CascadeClassifier cascadeClassifier = new CascadeClassifier(classifierFile.getPath());
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(img, faces, 1.1, 5, 2,
                        new Size(50, 50), new Size());
            } else {
                Log.e(LOGTAG, "Reading classifier file from " + classifierFile.getPath() + " failed");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Rect[] facesArray = faces.toArray();
        Log.d(LOGTAG, "Found " + facesArray.length + " face(s)");
        double largestSize = 0;
        Rect largestFace = null;
        for (int i = 0; i < facesArray.length; i++) {
            double faceSize = facesArray[i].size().height; // They are rectangles so it doesn't matter which dimension we use
            if (faceSize > largestSize) {
                largestSize = faceSize;
                largestFace = facesArray[i];
            }
            rectangle(img, facesArray[i].tl(), facesArray[i].br(), new Scalar(240, 15, 15, 255), 3);
        }

        if (largestFace != null) {
            Mat cropped = new Mat(img, largestFace);

            Imgcodecs.imwrite(context.getFilesDir().getPath() + "/" + "croppedFace.png", cropped);
            Imgcodecs.imwrite(path, img); // Rewrite the image to show in the screen with the one that has rectacles on the faces
            return new File(context.getFilesDir().getPath() + "/" + "croppedFace.png");
        }

        Log.d(LOGTAG, "Sendind photo");

        return null;
    }

}
