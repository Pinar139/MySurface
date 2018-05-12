
package com.example.pinarmnkl.mysurface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static com.example.pinarmnkl.mysurface.R.layout.ana_ekran;



public class giris_ekrani extends AppCompatActivity {
    Button button2;

    // Used to load the 'native-lib' library on application startup.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ana_ekran);
        Intent intent = getIntent();
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(giris_ekrani.this, MainActivity.class);
                startActivity(intent);
            }


        });


    }


}