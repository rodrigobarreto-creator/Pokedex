package com.example.interfazprueba;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.interfazprueba.scanner.ScannerActivity; // ← Import actualizado

public class MainActivity extends AppCompatActivity {

    private Button btnEnciclopedia;
    private Button btnPokeddle;
    private Button btnScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a los botones
        btnEnciclopedia = findViewById(R.id.btnEnciclopedia);
        btnPokeddle = findViewById(R.id.btnPokeddle);
        btnScanner = findViewById(R.id.btnScanner);

        // Abrir enciclopedia (PokedexActivity)
        btnEnciclopedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PokedexActivity.class);
                startActivity(intent);
            }
        });

        // Abrir el juego (Pokeddle)
        btnPokeddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        // Abrir el escáner (ScannerActivity)
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(intent);
            }
        });
    }
}
