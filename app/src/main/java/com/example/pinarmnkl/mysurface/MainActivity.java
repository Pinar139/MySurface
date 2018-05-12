package com.example.pinarmnkl.mysurface;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements  SurfaceHolder.Callback{
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean camcondition=false;
    Button test_resim,capturer,facedetection,test;
    ImageView grey;
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;

    private int cameraId = 0;
    final ArrayList<String> id_listem=new ArrayList<String>();

    android.hardware.Camera.PictureCallback rawCallback;
    android.hardware.Camera.ShutterCallback shutterCallback;
    android.hardware.Camera.PictureCallback jpegCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        capturer=(Button)findViewById(R.id.capturer);
        facedetection=(Button)findViewById(R.id.face_detection);
        test=(Button)findViewById(R.id.test);
        test_resim=(Button)findViewById(R.id.test_resim);
        surfaceHolder.addCallback(this);
        grey=(ImageView)findViewById(R.id.grey);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);
            }

        }

        //Resim çekme
        capturer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<10;i++){
                    camera.startPreview();
                    camera.takePicture(null, null,
                            null, mpicturecall);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(i==10)
                        camera.stopPreview();
                } }
        });
        //test resmi çekme

        //Resmin yüzünü kesme
        facedetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        face_detection.class);
                intent.putExtra("array_list", id_listem);
                startActivity(intent);
            }
        });

        //test butonu
       test.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                camera.startPreview();
                    camera.takePicture(null, null,
                            null, mpicturecall);
                     camera.stopPreview();
                 }

        });


        jpegCallback = new Camera.PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {

                FileOutputStream outStream = null;

                try {

                    outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));

                    outStream.write(data);

                    outStream.close();

                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);

                } catch (FileNotFoundException e) {

                    e.printStackTrace();

                } catch (IOException e) {

                    e.printStackTrace();

                } finally {

                }



                refreshCamera();

            }

        };

    }

    Camera.PictureCallback mpicturecall=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            FileOutputStream outstream=null;
            try{

                String ad=System.currentTimeMillis()+".jpg";
                outstream=new FileOutputStream("/sdcard/orjinal/"+ad);
               //idleri eliimzde tutuyoruz //facerecognization butonuyla yönlendirme yapıyoruz.
                id_listem.add(ad);
                // Toast.makeText(MainActivity.this, id_listem.toString(), Toast.LENGTH_SHORT).show();
                outstream.write(data);
                outstream.close();
                File file = new File(android.os.Environment.getExternalStorageDirectory(),"/orjinal/"+id_listem.get(0));
                final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                Resimler.testResmiBitmap=bitmap;
      //          grey.setImageBitmap(Resimler.testResmiBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
            }

        }
    };

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {

            // preview surface does not exist
            return;

        }
        // stop preview before making changes

        try {

            camera.stopPreview();

        } catch (Exception e) {

            // ignore: tried to stop a non-existent preview

        }

        try {

            camera.setPreviewDisplay(surfaceHolder);

            camera.startPreview();

        } catch (Exception e) {

        }

    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {


                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {camera.setDisplayOrientation(90);
        try {

            camera = Camera.open();
            camera.setDisplayOrientation(180);

        } catch (RuntimeException e) {
            System.err.println(e);
            return;

        }
        Camera.Parameters param;

        param = camera.getParameters();
        param.setPreviewSize(352, 288);
        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e) {
            System.err.println(e);
            return;

        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        refreshCamera();


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

        camera.stopPreview();
        camera.release();
        camera=null;
        camcondition=false;


    }


}
