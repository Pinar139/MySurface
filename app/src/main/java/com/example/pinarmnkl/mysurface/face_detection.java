package com.example.pinarmnkl.mysurface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 5.03.2018.
 */

        //resimde yüzü bulma işlemini yapıyor
    public class face_detection extends Activity {

    //ImageView resim, resim2;

    Button buti,test;
    ArrayList<String> arr;
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);*/

        Bundle b = getIntent().getExtras();
        if (b != null) {
            arr =(ArrayList<String>) b.getStringArrayList("array_list");
            Toast.makeText(this, arr.toString(), Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(this, "boş döndü", Toast.LENGTH_SHORT).show();

        //Firebase bağlantısı

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        buti = (Button) findViewById(R.id.face_detection);
        test=(Button)findViewById(R.id.test);

        //DOSYAYA KAYDETTİĞİMİZ RESMİ ÇEKİP BİTMAP DOSYASINA ATIYORUZ
        for (int i = 0; i <arr.size(); i++) {
            File file = new File(android.os.Environment.getExternalStorageDirectory(),"/orjinal/"+ arr.get(i));
            final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            face_Detector(bitmap,arr.get(i));
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


    }


    System.out.println("resim bitmap"+Resimler.testResmiBitmap);

    face_Detector(Resimler.testResmiBitmap,"test");
    //test dosyasına id leri yollarız yüzü çekip kaydettiği
   test.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(face_detection.this,
                    Test_activity.class);
            intent.putExtra("array_list", arr);
            startActivity(intent);
        }
    });
    }

    public void face_Detector(Bitmap bitmap,String ad) {

        //Resimi çevirmek için matris sınıfından nesne türetip ne kadar çevireceğimizi
        //postRotateye gireriz. ve rotated bitmapine aktarırız. not: bizim resmimiz +90 geliyordu o yüzden -90 atadık
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);

        final Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);

        final Paint rectPaint = new Paint();
        rectPaint.setStrokeWidth(6);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);

        final Bitmap tempBitmap = Bitmap.createBitmap(rotated.getWidth(), rotated.getHeight(), Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(tempBitmap);

        canvas.drawBitmap(rotated, 0, 0, null);

        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())

                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        if (!faceDetector.isOperational()) {

            Toast.makeText(face_detection.this, "FaceDedector could not be set up your device", Toast.LENGTH_SHORT).show();
            return;

        }

        Frame frame = new Frame.Builder().setBitmap(rotated).build();

        SparseArray<Face> sparceArray = faceDetector.detect(frame);
        for (int i = 0; i < sparceArray.size(); i++) {

            Face face = sparceArray.valueAt(i);
            float x1 = face.getPosition().x;
            float y1 = face.getPosition().y;
            float x2 = x1 + face.getWidth();
            float y2 = y1 + face.getHeight();

            RectF rectF = new RectF(x1, y1, x2, y2);
            canvas.drawRoundRect(rectF, 2, 2, rectPaint);

            //KIRPMA İŞLEMİNİ RESMİN ÜSTÜNE YAPIYOR
            assert (rectF.left < rectF.right && rectF.top < rectF.bottom);
            Bitmap resultBmp = Bitmap.createBitmap((int) rectF.right - (int) rectF.left, (int) rectF.bottom - (int) rectF.top, Bitmap.Config.ARGB_8888);
            new Canvas(resultBmp).drawBitmap(tempBitmap, -rectF.left, -rectF.top, null);
            FileOutputStream out = null;
            try {
                String random="/storage/emulated/0/" +"/databases/" +ad;
                out = new FileOutputStream(random);
                filePath= Uri.fromFile(new File(random));
                uploadImage(filePath);
                resultBmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance

                Resimler.facedet=resultBmp;

                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

       // resim2.setImageBitmap(tempBitmap);
    }
    private void uploadImage(Uri filePath)
    {   this.filePath=filePath;
        if(filePath !=null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("kaydediliyor....");

            progressDialog.show();
            String androidId = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID) + Build.SERIAL;
            System.out.println("android seri numara"+androidId);

            StorageReference ref =storageReference.child("databases/");
           //randomu yukarı kaldırdım

            StorageReference serino=ref.child(androidId+"/" +UUID.randomUUID().toString());
            // arr2 sınıfında tüm firebase linklerini tuttum

            serino.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(face_detection.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(face_detection.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                            progressDialog.setMessage("Uploeded%");


                        }
                    });

        }



    }


}




































