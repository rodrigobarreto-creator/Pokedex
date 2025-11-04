package com.example.interfazprueba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultText = findViewById(R.id.resultText);
        TextView pokemonName = findViewById(R.id.pokemonName);
        TextView pokemonType = findViewById(R.id.pokemonType);
        TextView pokemonColor = findViewById(R.id.pokemonColor);
        TextView pokemonGeneration = findViewById(R.id.pokemonGeneration);
        ImageView pokemonImage = findViewById(R.id.pokemonImage);
        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button mainMenuButton = findViewById(R.id.mainMenuButton);
        Button backButton = findViewById(R.id.backButton);

        // Botón para volver al menú principal
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Intent intent = getIntent();
        boolean won = intent.getBooleanExtra("WON", false);
        String pokemon = intent.getStringExtra("POKEMON");
        String typeDisplay = intent.getStringExtra("TYPE");
        String color = intent.getStringExtra("COLOR");
        String generation = intent.getStringExtra("GENERATION");
        String imageName = intent.getStringExtra("IMAGE_NAME");
        int attempts = intent.getIntExtra("ATTEMPTS", 0);

        if (won) {
            resultText.setText("¡Felicidades!\nAdivinaste en " + attempts + " intentos");
            resultText.setTextColor(ContextCompat.getColor(this, R.color.correct_position));
        } else {
            resultText.setText("¡Game Over!\nEl Pokémon era:");
            resultText.setTextColor(ContextCompat.getColor(this, R.color.wrong_letter));
        }

        pokemonName.setText(pokemon);
        pokemonType.setText("Tipo: " + typeDisplay);
        pokemonColor.setText("Color: " + color);
        pokemonGeneration.setText("Generación: " + generation);

        // Asignar color según el tipo del Pokémon
        setTypeColor(pokemonType, typeDisplay);

        // Cargar la imagen del Pokémon
        if (imageName != null) {
            int resourceId = getResources().getIdentifier(
                    imageName,
                    "drawable",
                    getPackageName()
            );
            if (resourceId != 0) {
                pokemonImage.setImageResource(resourceId);
                pokemonImage.setVisibility(View.VISIBLE);
            }
        }

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAgain();
            }
        });

        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainMenu();
            }
        });
    }

    private void setTypeColor(TextView typeTextView, String typeDisplay) {
        String fullText = "Tipo: " + typeDisplay;
        android.text.SpannableString spannable = new android.text.SpannableString(fullText);

        int startIndex = 6;

        if (typeDisplay.contains("/")) {
            String[] types = typeDisplay.split("/");
            int type1End = startIndex + types[0].length();
            int type2Start = type1End + 1;
            int type2End = type2Start + types[1].length();

            setTypeColorSpan(spannable, types[0], startIndex, type1End);
            setTypeColorSpan(spannable, types[1], type2Start, type2End);

        } else {
            setTypeColorSpan(spannable, typeDisplay, startIndex, fullText.length());
        }

        typeTextView.setText(spannable);
    }

    private void setTypeColorSpan(android.text.SpannableString spannable, String type, int start, int end) {
        switch (type.toUpperCase()) {
            case "ELÉCTRICO":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_electric)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "FUEGO":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_fire)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "AGUA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_water)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "PLANTA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_grass)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "NORMAL":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_normal)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "PSÍQUICO":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_psychic)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "LUCHA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_fighting)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "DRAGÓN":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_dragon)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "FANTASMA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_ghost)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "ROCA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_rock)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "ACERO":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_steel)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "VOLADOR":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_flying)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "TIERRA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_ground)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "SINIESTRO":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_dark)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case "HADA":
                spannable.setSpan(new android.text.style.ForegroundColorSpan(
                                ContextCompat.getColor(this, R.color.type_fairy)), start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            default:
                spannable.setSpan(new android.text.style.ForegroundColorSpan(Color.BLACK),
                        start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
        }
    }

    private void playAgain() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    private void mainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
