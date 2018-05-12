package com.example.pinarmnkl.mysurface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Test_activity extends AppCompatActivity {
    /*Resimlerin çekildiği yerdir

      *veri setleri oluşturulacaktır
      *yapılmamsı istenen
      * setler oluşturulduktan sonra algoritmaya yönlendir
      */


    ArrayList<String> arr;
    ImageView grey;
    static final double epsilon = 0.01;     // convergency threshold
    static double lambda;                   // eigenvalue
    // double esik =2.5467846830396847E-6;

    double en_kck;
    double[] p = new double[10];
    Button test, capturer, facedetection;
    double esik=0.000002;
    ImageView gelenResim;
    // initial eigenvector

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grey = (ImageView) findViewById(R.id.grey);
        test = (Button) findViewById(R.id.test);
        facedetection = (Button) findViewById(R.id.face_detection);
        capturer = (Button) findViewById(R.id.capturer);
        //  imageView=(ImageView)findViewById(R.id.imageView);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            arr = (ArrayList<String>) b.getStringArrayList("array_list");
            Toast.makeText(this, arr.toString(), Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(this, "boş döndü", Toast.LENGTH_SHORT).show();

        double[][] tum_veri = new double[10][10000];
        for (int i = 0; i < 10; i++) {
            File file = new File(Environment.getExternalStorageDirectory(), "/databases/" + arr.get(i));
            final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            double verim[] = getResizedBitmap(bitmap, 100, 100);
            for (int j = 0; j < 10000; j++) {
                tum_veri[i][j] = verim[j];
                System.out.println("tum veri :[" + i + "][" + j + "]" + tum_veri[i][j]);

            }
        }


        double[][] ort_veri = ortalamaya_göre_ver_Seti(transpose(tum_veri));

        for (int i = 0; i < 10; i++) {
            p[i] = 1;

        }

        double[][] L = matris_carpimi(transpose(ort_veri), ort_veri);

        double[] q;
        double[][] v_vektör = new double[20][10];
        System.out.println();
        for (int i = 0; i < p.length; i++) System.out.print("p[" + i + "]\t\t");

        System.out.println("lambda");

        int k = 0;
        do {
            for (int j = 0; j < p.length; j++) {
                System.out.format("%f \t değer", p[j]);
                v_vektör[k][j] = p[j];

            }

            // show the current vector

            System.out.format("%f lambda \n", lambda);                             // and the eigenvalue (lambda)
            // and the eigenvalue (lambda)


            q = p;
            p = AxP(L, q);
            lambda = norm(p);
            p = PxL(p, 1 / lambda);


            k++;
        }
        while (norm(PminusQ(p, q)) > epsilon);
        //tek boyutta olan vektörümü ikki boyuta cevirdim

        for (int i = 0; i < v_vektör.length; i++) {
            for (int j = 0; j < v_vektör[0].length; j++) {

                System.out.println("vektör[" + i + "][" + j + "]= " + v_vektör[i][j]);
            }

        }
        double[][] u_vekörü = matris_carpimi(ort_veri, transpose(v_vektör));
        double[][] v_k = matris_carpimi(transpose(u_vekörü), ort_veri);


        //orjinal_resim(vektör,sonuc_verisi);

        //TEST RESMİ İÇİN DATAYI AL

        double[][] test_tum_veri = new double[1][10000];
        for (int i = 0; i < 1; i++) {
            //File file = new File(android.os.Environment.getExternalStorageDirectory(), "/databases/" + "1.jpg");
            //final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            double verim[] = getResizedBitmap(Resimler.facedet, 100, 100);
            for (int j = 0; j < 10000; j++) {
                test_tum_veri[i][j] = verim[j];
                System.out.println("testin ortalmaa verisi :[" + i + "][" + j + "]" + test_tum_veri[i][j]);

            }
        }

        double[][] ortalama = ortalama(transpose(tum_veri));
        double[][] test_V_vektoru = v_vektoru_test(transpose(test_tum_veri), ortalama);
        double[][] v_vektor_test = matris_carpimi(transpose(u_vekörü), test_V_vektoru);


        System.out.println("test vektörü+ satır " + v_vektor_test.length + " sütün" + v_vektor_test[0].length);
        System.out.println("v_k+ satır " + v_k.length + " sütün" + v_k[0].length);

/*
        double [][]sonuc_verisi=matris_carpimi(transpose(u_vekörü),v_k);

        double toplam=0;
        double[][] toplanmıs_veri=new double [sonuc_verisi.length][0];
        for(int i=0;i<sonuc_verisi.length;i++){
            toplam=0;
            for(int j=0;j<sonuc_verisi[0].length;j++){

                toplam=toplam+sonuc_verisi[i][j];


            }
            toplanmıs_veri[i][0]=toplam+ortalama[i][0];
        }


        if(toplanmıs_veri==test_tum_veri)
        {
            Toast.makeText(this, "ne olmuş olaki0", Toast.LENGTH_SHORT).show();
        }

            else
        {
            Toast.makeText(this, "tabiki tanımadıııııııı!!!!!!!!!", Toast.LENGTH_SHORT).show();
        }
*/

        double[][] e_k = E_K(v_vektor_test, v_k);


        en_kck = e_k[0][0];
        e_k = transpose(e_k);
        System.out.println("Ek_SATIR" + e_k.length + "ek_sütun" + e_k[0].length);


        for (int j = 0; j < 20; j++)
            if (e_k[0][j] != 0)
                if (en_kck > e_k[0][j])
                    en_kck = e_k[0][j];

        System.out.println("Gelen" + en_kck);

        if(esik<en_kck) {
            System.out.println("tanınamadı" + en_kck);
            Toast.makeText(this, "tanınamadı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Test_activity.this,
                    giris_ekrani.class);

            startActivity(intent);
        }
        else {
            Intent intent = new Intent(Test_activity.this,
                    basarili.class);
            Toast.makeText(this, "giriş başarılı", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            System.out.println("en kücük e_k degeri" + en_kck + " resim tanındı");
        }

    }


    // Computes P = A x P (A - matrix, P - column vector)
    public static double[] AxP(double[][] a, double[] p) {
        double[] q = new double[10];
        double s;
        for (int i = 0; i < 10; i++) {
            q[i] = 0;
            for (int j = 0; j < 10; j++)
                q[i] = q[i] + a[i][j] * p[j];
        }
        return q;
    }

    // Computes P = P x L (P - vector, L - scalar)
    public static double[] PxL(double[] p, double lambda) {
        double[] q = new double[p.length];
        for (int i = 0; i < p.length; i++)
            q[i] = p[i] * lambda;
        return q;
    }

    // Computes P-Q (P and Q - vectors)
    public static double[] PminusQ(double[] p, double[] q) {
        double[] r = new double[p.length];
        for (int i = 0; i < p.length; i++)
            r[i] = p[i] - q[i];
        return r;
    }

    // Computes Euclidean norm of P
    public static double norm(double[] p) {
        double s = 0;
        for (int i = 0; i < p.length; i++)
            s = s + p[i] * p[i];
        return Math.sqrt(s);
    }

    //bitmapi tekrar boyutlandır->gri yap->int->double
    //tüm ön işlemleri içerir
    //eğer tanımlamada hata olursa resmi gri yapmaktan vazgecilebilir
    public static double[] getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        //Custom color matrix
        float[] x = new float[]{
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0,};

        Bitmap dest = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(x);
        paint.setColorFilter(filter);
        canvas.drawBitmap(resizedBitmap, 0, 0, paint);

        int[] intArray = new int[dest.getWidth() * dest.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        dest.getPixels(intArray, 0, dest.getWidth(), 0, 0, dest.getWidth(), dest.getHeight());

        double[] doub = new double[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            doub[i] = intArray[i];
        }
        return doub;

    }

    //pca algoritma
    //1. adım ortalama cikarma
    public double[][] ortalamaya_göre_ver_Seti(double[][] veri) {


        System.out.println("ortalamaya göre fonk satır sayısı = " + veri.length + "sütun sayısı= " + veri[0].length);

        double[][] ortalama = new double[veri.length][1];
        for (int i = 0; i < veri.length; i++) {
            double toplam = 0;
            //System.out.println("değer"+i+veri[i]);
            for (int j = 0; j < veri[0].length; j++) {
                //değerleri ortalama ile çıkar
                toplam = toplam + veri[i][j];

            }
            ortalama[i][0] = toplam / veri[0].length;

        }
        for (int i = 0; i < veri.length; i++) {
            for (int j = 0; j < veri[0].length; j++) {
                veri[i][j] = veri[i][j] - ortalama[i][0];
                //System.out.println("değiştirilmiş veri"+i+"."+j+". "+veri[i][j]);
            }
        }
        return veri;

    }

    public double[][] ortalama(double[][] veri) {

        double[][] ortalama = new double[veri.length][1];
        for (int i = 0; i < veri.length; i++) {
            double toplam = 0;
            //System.out.println("değer"+i+veri[i]);
            for (int j = 0; j < veri[0].length; j++) {
                //değerleri ortalama ile çıkar
                toplam = toplam + veri[i][j];

            }
            ortalama[i][0] = toplam / veri[0].length;

        }
        return ortalama;

    }

    //nitelik * ortalamdan çıkartılmış veri
    public double[][] matris_carpimi(double[][] vektor, double[][] ortalama) {

        double[][] c = new double[vektor.length][ortalama[0].length];

        //Çarpma işlemi yapılıyor.
        for (int i = 0; i < vektor.length; i++) {
            for (int j = 0; j < ortalama[0].length; j++) {
                for (int k = 0; k < ortalama.length; k++) {
                    c[i][j] = c[i][j] + vektor[i][k] * ortalama[k][j];
                }
            }
        }

        //Sonuç ekrana yazdırılıyor.
      /*  for(int i=0;i<c.length;i++){
            for(int j=0;j<c[0].length;j++){
                System.out.print(c[i][j]+" ");
            }
            System.out.println();
        }*/
        return c;


    }

    //a[i][j]->a[j][i] yapar yani transpose alınır eğer nxm gibi ise boyut a[i][j] -> transpose[j][i] olarak çıktı alınır
    public static double[][] transpose(double arr[][]) {

        double ret[][] = new double[arr[0].length][arr.length];

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                ret[j][i] = arr[i][j];
                // System.out.println(" Transpoz["+j+"][ "+i+"]"+ret[j][i]);

            }
        }

        return ret;
    }

    public double[][] v_vektoru_test(double[][] test_verisi, double[][] ortalama) {
        double[][] v_vektor = new double[test_verisi.length][ortalama[0].length];

        for (int i = 0; i < test_verisi.length; i++) {
            v_vektor[i][0] = test_verisi[i][0] - ortalama[i][0];

        }
        return v_vektor;
    }

    public double[][] orjinal_resim(double[][] nitelik_vektörü, double[][] sonucverisi) {
        double[][] c;

        c = matris_carpimi(transpose(nitelik_vektörü), sonucverisi);
        //Sonuç ekrana yazdırılıyor.
        for (int i = 0; i < nitelik_vektörü.length; i++) {
            for (int j = 0; j < sonucverisi[0].length; j++) {
                System.out.print("orjinal resim içindeyiz" + c[i][j] + " ");
            }
            System.out.println();
        }
        return c;


    }

    public double[][] E_K(double[][] v_test, double[][] v_k) {
        double[][] e_k = new double[v_test.length][1];
        double cikarma, toplam = 0;
        for (int i = 0; i < v_test.length; i++) {
            cikarma = 0;
            toplam = 0;
            for (int j = 0; j < v_k[0].length; j++) {

                cikarma = v_test[i][0] - v_k[i][j];
                toplam = toplam + (cikarma * cikarma);

            }
            e_k[i][0] = Math.sqrt(toplam);
            System.out.println("e_k [" + i + "][" + "0]" + e_k[i][0]);
            e_k[i][0] = Math.sqrt(toplam) / 10000000;
            System.out.println("e_k [" + i + "][" + "0]" + e_k[i][0]);

            System.out.println("e_k [" + i + "][" + "0]" + new DecimalFormat("##.##").format(e_k[i][0]));
        }
        return e_k;



    }


}

