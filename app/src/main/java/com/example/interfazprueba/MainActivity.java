package com.example.interfazprueba;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnEnciclopedia;
    private Button btnScanner;
    private Button btnPokeddle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEnciclopedia = findViewById(R.id.btnEnciclopedia);
        btnScanner = findViewById(R.id.btnScanner);
        btnPokeddle = findViewById(R.id.btnPokeddle);

        btnEnciclopedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir actividad de Enciclopedia
                // Ejemplo:
                // Intent intent = new Intent(MainActivity.this, EnciclopediaActivity.class);
                // startActivity(intent);
            }
        });

        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir actividad de Scanner
            }
        });

        btnPokeddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir actividad de Pokeddle
            }
        });
    }
}