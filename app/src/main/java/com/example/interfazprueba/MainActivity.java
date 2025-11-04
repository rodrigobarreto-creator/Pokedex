package com.example.interfazprueba;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnEnciclopedia;
    private Button btnPokeddle;
    private Button btnScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencia a los botones
        btnEnciclopedia = findViewById(R.id.btnEnciclopedia);
        btnPokeddle = findViewById(R.id.btnPokeddle);
        btnScanner = findViewById(R.id.btnScanner);

        // Listener para abrir PokedexActivity
        btnEnciclopedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PokedexActivity.class);
                startActivity(intent);
            }
        });

        // Listener para abrir GameActivity (Pokeddle) - ¡DIRECTO AL JUEGO!
        btnPokeddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        // Listener para el botón Scanner
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.widget.Toast.makeText(MainActivity.this, "Función escáner próximamente", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
