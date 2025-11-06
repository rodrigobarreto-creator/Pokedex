package com.example.interfazprueba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private EditText inputEditText;
    private Button submitButton;
    private Button hintButton;
    private TextView attemptsTextView;
    private TextView hintTextView;
    private Button backButton;
    private ImageView topPokemonImage;
    private ImageView bottomPokemonImage;

    private Pokemon targetPokemon;
    private List<Pokemon> pokemonList;
    private int currentAttempt = 0;
    private final int MAX_ATTEMPTS = 10;
    private List<TextView[]> guessRows;
    private boolean hintUsed = false;

    private Handler imageHandler = new Handler();
    private Random random = new Random();

    private int cellWidth;
    private int cellHeight;
    private int cellTextSize;
    private int cellDataTextSize; // Tamaño de texto más pequeño para datos

    // Anchors para las columnas (ahora se calculan dinámicamente)
    private int[] columnWidths;
    private String[] columnTitles = {"NOMBRE", "TIPO 1", "TIPO 2", "COLOR", "FASE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        calculateResponsiveDimensions();
        initializePokemonList();
        selectRandomPokemon();
        setupUI();
        setupGridWithTitles();
        startImageRotation();
    }

    private void calculateResponsiveDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;

        // Calcular anchos basados en el ancho de pantalla
        // FASE es más angosta que las demás columnas
        int standardWidth = (int) (screenWidth * 0.18);
        int phaseWidth = (int) (screenWidth * 0.14); // FASE más angosta

        cellHeight = (int) (displayMetrics.heightPixels * 0.035);
        cellTextSize = (int) (displayMetrics.widthPixels * 0.012);
        cellDataTextSize = (int) (displayMetrics.widthPixels * 0.010); // Texto más pequeño para datos

        // Asegurar mínimos
        if (standardWidth < 150) standardWidth = 150;
        if (phaseWidth < 120) phaseWidth = 120;
        if (cellHeight < 25) cellHeight = 25;
        if (cellTextSize < 4) cellTextSize = 4;
        if (cellDataTextSize < 3) cellDataTextSize = 3;

        // Actualizar columnWidths con FASE más angosta
        columnWidths = new int[]{standardWidth, standardWidth, standardWidth, standardWidth, phaseWidth};
    }

    private void setupGridWithTitles() {
        guessRows = new ArrayList<>();
        gridLayout.removeAllViews();
        gridLayout.setRowCount(11);

        // PRIMERA FILA: TÍTULOS DE COLUMNAS (texto normal)
        for (int col = 0; col < 5; col++) {
            TextView titleTextView = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(0);
            params.columnSpec = GridLayout.spec(col);
            params.width = columnWidths != null && col < columnWidths.length ? columnWidths[col] : cellWidth;
            params.height = cellHeight;
            params.setMargins(1, 1, 1, 1);

            titleTextView.setLayoutParams(params);
            titleTextView.setText(columnTitles[col]);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, cellTextSize); // Tamaño normal para títulos
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.pokemon_red));
            titleTextView.setGravity(android.view.Gravity.CENTER);
            titleTextView.setPadding(4, 4, 4, 4);
            titleTextView.setTypeface(null, android.graphics.Typeface.BOLD);

            gridLayout.addView(titleTextView);
        }

        // FILAS 1-10: DATOS DEL JUEGO (texto más pequeño)
        for (int row = 1; row <= MAX_ATTEMPTS; row++) {
            TextView[] rowViews = new TextView[5];
            for (int col = 0; col < 5; col++) {
                TextView textView = new TextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.width = columnWidths != null && col < columnWidths.length ? columnWidths[col] : cellWidth;
                params.height = cellHeight;
                params.setMargins(1, 1, 1, 1);

                textView.setLayoutParams(params);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, cellDataTextSize); // Texto más pequeño para datos
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setPadding(4, 4, 4, 4);
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
                textView.setMaxLines(1);
                textView.setEllipsize(TextUtils.TruncateAt.END);

                gridLayout.addView(textView);
                rowViews[col] = textView;
            }
            guessRows.add(rowViews);
        }
    }

    private void updateGrid(GuessResult result) {
        TextView[] currentRow = guessRows.get(currentAttempt);

        // Pokémon (NOMBRE)
        if (result.pokemonName != null) {
            currentRow[0].setText(result.pokemonName);
            if (result.isCorrectPokemon) {
                currentRow[0].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_position));
                currentRow[0].setTextColor(Color.WHITE);
            } else {
                currentRow[0].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_letter));
                currentRow[0].setTextColor(Color.WHITE);
            }
        } else {
            currentRow[0].setText("-");
            currentRow[0].setBackgroundColor(Color.LTGRAY);
            currentRow[0].setTextColor(Color.DKGRAY);
        }

        // Tipo 1
        if (result.pokemonType1 != null) {
            currentRow[1].setText(result.pokemonType1);
            if (result.isCorrectType1) {
                currentRow[1].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_position));
                currentRow[1].setTextColor(Color.WHITE);
            } else {
                currentRow[1].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_letter));
                setGridTypeColor(currentRow[1], result.pokemonType1);
            }
        } else {
            currentRow[1].setText("-");
            currentRow[1].setBackgroundColor(Color.LTGRAY);
            currentRow[1].setTextColor(Color.DKGRAY);
        }

        // Tipo 2
        if (result.pokemonType2 != null && !result.pokemonType2.isEmpty()) {
            currentRow[2].setText(result.pokemonType2);
            if (result.isCorrectType2) {
                currentRow[2].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_position));
                currentRow[2].setTextColor(Color.WHITE);
            } else {
                currentRow[2].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_letter));
                setGridTypeColor(currentRow[2], result.pokemonType2);
            }
        } else {
            currentRow[2].setText("-");
            currentRow[2].setBackgroundColor(Color.LTGRAY);
            currentRow[2].setTextColor(Color.DKGRAY);
        }

        // Color
        if (result.pokemonColor != null) {
            currentRow[3].setText(result.pokemonColor);
            if (result.isCorrectColor) {
                currentRow[3].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_position));
                currentRow[3].setTextColor(Color.WHITE);
            } else {
                currentRow[3].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_letter));
                currentRow[3].setTextColor(Color.WHITE);
            }
        } else {
            currentRow[3].setText("-");
            currentRow[3].setBackgroundColor(Color.LTGRAY);
            currentRow[3].setTextColor(Color.DKGRAY);
        }

        // Fase (reemplaza Generación)
        if (result.pokemonPhase != null) {
            currentRow[4].setText(result.pokemonPhase);
            if (result.isCorrectPhase) {
                currentRow[4].setBackgroundColor(ContextCompat.getColor(this, R.color.correct_position));
                currentRow[4].setTextColor(Color.WHITE);
            } else {
                currentRow[4].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_letter));
                currentRow[4].setTextColor(Color.WHITE);
            }
        } else {
            currentRow[4].setText("-");
            currentRow[4].setBackgroundColor(Color.LTGRAY);
            currentRow[4].setTextColor(Color.DKGRAY);
        }

        // Animación
        for (TextView tv : currentRow) {
            tv.setScaleX(0.8f);
            tv.setScaleY(0.8f);
            tv.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
        }
    }

    private void startImageRotation() {
        imageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePokemonImages();
                imageHandler.postDelayed(this, 4000);
            }
        }, 1000);
    }

    private void updatePokemonImages() {
        if (topPokemonImage == null || bottomPokemonImage == null) return;

        Pokemon topPokemon = getRandomPokemon();
        Pokemon bottomPokemon = getRandomPokemon();

        while (bottomPokemon.getName().equals(topPokemon.getName())) {
            bottomPokemon = getRandomPokemon();
        }

        int topResourceId = getResources().getIdentifier(topPokemon.getImageName(), "drawable", getPackageName());
        if (topResourceId != 0) {
            topPokemonImage.setImageResource(topResourceId);
        }

        int bottomResourceId = getResources().getIdentifier(bottomPokemon.getImageName(), "drawable", getPackageName());
        if (bottomResourceId != 0) {
            bottomPokemonImage.setImageResource(bottomResourceId);
        }
    }

    private Pokemon getRandomPokemon() {
        return pokemonList.get(random.nextInt(pokemonList.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageHandler.removeCallbacksAndMessages(null);
    }

    private void initializePokemonList() {
        pokemonList = Arrays.asList(
                // LÍNEA BULBASAUR - Fases: BÁSICO, 1, 2
                new Pokemon("BULBASAUR", "PLANTA", "VENENO", "VERDE", "BÁSICO", "bulbasaur"),
                new Pokemon("IVYSAUR", "PLANTA", "VENENO", "VERDE", "1", "ivysaur"),
                new Pokemon("VENUSAUR", "PLANTA", "VENENO", "VERDE", "2", "venusaur"),

                // LÍNEA CHARMANDER - Fases: BÁSICO, 1, 2
                new Pokemon("CHARMANDER", "FUEGO", "NARANJA", "BÁSICO", "charmander"),
                new Pokemon("CHARMELEON", "FUEGO", "ROJO", "1", "charmeleon"),
                new Pokemon("CHARIZARD", "FUEGO", "VOLADOR", "NARANJA", "2", "charizard"),

                // LÍNEA SQUIRTLE - Fases: BÁSICO, 1, 2
                new Pokemon("SQUIRTLE", "AGUA", "AZUL", "BÁSICO", "squirtle"),
                new Pokemon("WARTORTLE", "AGUA", "AZUL", "1", "wartortle"),
                new Pokemon("BLASTOISE", "AGUA", "AZUL", "2", "blastoise"),

                // LÍNEA CATERPIE - Fases: BÁSICO, 1, 2
                new Pokemon("CATERPIE", "BICHO", "VERDE", "BÁSICO", "caterpie"),
                new Pokemon("METAPOD", "BICHO", "VERDE", "1", "metapod"),
                new Pokemon("BUTTERFREE", "BICHO", "VOLADOR", "MORADO", "2", "butterfree"),

                // LÍNEA PIDGEY - Fases: BÁSICO, 1, 2
                new Pokemon("PIDGEY", "NORMAL", "VOLADOR", "MARRÓN", "BÁSICO", "pidgey"),
                new Pokemon("PIDGEOTTO", "NORMAL", "VOLADOR", "MARRÓN", "1", "pidgeotto"),
                new Pokemon("PIDGEOT", "NORMAL", "VOLADOR", "MARRÓN", "2", "pidgeot"),

                // LÍNEA RATTATA - Fases: BÁSICO, 1
                new Pokemon("RATTATA", "NORMAL", "MORADO", "BÁSICO", "rattata"),
                new Pokemon("RATICATE", "NORMAL", "MARRÓN", "1", "raticate"),

                // LÍNEA EKANS - Fases: BÁSICO, 1
                new Pokemon("EKANS", "VENENO", "MORADO", "BÁSICO", "ekans"),
                new Pokemon("ARBOK", "VENENO", "MORADO", "1", "arbok"),

                // LÍNEA PIKACHU - Fases: BÁSICO, 1
                new Pokemon("PIKACHU", "ELÉCTRICO", "AMARILLO", "BÁSICO", "pikachu"),
                new Pokemon("RAICHU", "ELÉCTRICO", "NARANJA", "1", "raichu"),

                // LÍNEA CLEFAIRY - Fases: BÁSICO, 1
                new Pokemon("CLEFAIRY", "HADA", "ROSA", "BÁSICO", "clefairy"),
                new Pokemon("CLEFABLE", "HADA", "ROSA", "1", "clefable"),

                // LÍNEA JIGGLYPUFF - Fases: BÁSICO, 1
                new Pokemon("JIGGLYPUFF", "HADA", "ROSA", "BÁSICO", "jigglypuff"),
                new Pokemon("WIGGLYTUFF", "HADA", "ROSA", "1", "wigglytuff"),

                // LÍNEA ZUBAT - Fases: BÁSICO, 1
                new Pokemon("ZUBAT", "VENENO", "VOLADOR", "MORADO", "BÁSICO", "zubat"),
                new Pokemon("GOLBAT", "VENENO", "VOLADOR", "MORADO", "1", "golbat"),

                // LÍNEA ODDISH - Fases: BÁSICO, 1, 2
                new Pokemon("ODDISH", "PLANTA", "VENENO", "AZUL", "BÁSICO", "oddish"),
                new Pokemon("GLOOM", "PLANTA", "VENENO", "AZUL", "1", "gloom"),
                new Pokemon("VILEPLUME", "PLANTA", "VENENO", "AZUL", "2", "vileplume"),

                // LÍNEA ABRA - Fases: BÁSICO, 1, 2
                new Pokemon("ABRA", "PSÍQUICO", "AMARILLO", "BÁSICO", "abra"),
                new Pokemon("KADABRA", "PSÍQUICO", "AMARILLO", "1", "kadabra"),
                new Pokemon("ALAKAZAM", "PSÍQUICO", "AMARILLO", "2", "alakazam"),

                // LÍNEA MACHOP - Fases: BÁSICO, 1, 2
                new Pokemon("MACHOP", "LUCHA", "GRIS", "BÁSICO", "machop"),
                new Pokemon("MACHOKE", "LUCHA", "GRIS", "1", "machoke"),
                new Pokemon("MACHAMP", "LUCHA", "GRIS", "2", "machamp"),

                // LÍNEA GEODUDE - Fases: BÁSICO, 1, 2
                new Pokemon("GEODUDE", "ROCA", "TIERRA", "MARRÓN", "BÁSICO", "geodude"),
                new Pokemon("GRAVELER", "ROCA", "TIERRA", "MARRÓN", "1", "graveler"),
                new Pokemon("GOLEM", "ROCA", "TIERRA", "MARRÓN", "2", "golem"),

                // LÍNEA GASTLY - Fases: BÁSICO, 1, 2
                new Pokemon("GASTLY", "FANTASMA", "VENENO", "MORADO", "BÁSICO", "gastly"),
                new Pokemon("HAUNTER", "FANTASMA", "VENENO", "MORADO", "1", "haunter"),
                new Pokemon("GENGAR", "FANTASMA", "VENENO", "MORADO", "2", "gengar"),

                // LÍNEA GROWLITHE (ARCANINE) - Fases: BÁSICO, 1
                new Pokemon("GROWLITHE", "FUEGO", "NARANJA", "BÁSICO", "growlithe"),
                new Pokemon("ARCANINE", "FUEGO", "NARANJA", "1", "arcanine"),

                // LÍNEA DRATINI (DRAGONITE) - Fases: BÁSICO, 1, 2
                new Pokemon("DRATINI", "DRAGÓN", "AZUL", "BÁSICO", "dratini"),
                new Pokemon("DRAGONAIR", "DRAGÓN", "AZUL", "1", "dragonair"),
                new Pokemon("DRAGONITE", "DRAGÓN", "VOLADOR", "NARANJA", "2", "dragonite"),

                // LÍNEA MAGIKARP (GYARADOS) - Fases: BÁSICO, 1
                new Pokemon("MAGIKARP", "AGUA", "ROJO", "BÁSICO", "magikarp"),
                new Pokemon("GYARADOS", "AGUA", "VOLADOR", "AZUL", "1", "gyarados"),

                // POKÉMON ÚNICOS LEGENDARIOS - Fase: BÁSICO
                new Pokemon("SNORLAX", "NORMAL", "AZUL", "BÁSICO", "snorlax"),
                new Pokemon("MEWTWO", "PSÍQUICO", "MORADO", "BÁSICO", "mewtwo"),
                new Pokemon("MEW", "PSÍQUICO", "ROSA", "BÁSICO", "mew")
        );
    }

    private void selectRandomPokemon() {
        targetPokemon = pokemonList.get(random.nextInt(pokemonList.size()));
    }

    private void setupUI() {
        gridLayout = findViewById(R.id.gridLayout);
        inputEditText = findViewById(R.id.inputEditText);
        submitButton = findViewById(R.id.submitButton);
        hintButton = findViewById(R.id.hintButton);
        attemptsTextView = findViewById(R.id.attemptsTextView);
        hintTextView = findViewById(R.id.hintTextView);
        backButton = findViewById(R.id.backButton);
        topPokemonImage = findViewById(R.id.topPokemonImage);
        bottomPokemonImage = findViewById(R.id.bottomPokemonImage);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processGuess();
            }
        });

        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHint();
            }
        });

        attemptsTextView.setText("Intentos: 0/" + MAX_ATTEMPTS);
        inputEditText.setBackgroundColor(Color.WHITE);
        inputEditText.setTextColor(Color.BLACK);
        inputEditText.setHint("Ej: PIKACHU, FUEGO, AZUL, BÁSICO");

        updatePokemonImages();
    }

    private void showHint() {
        if (!hintUsed) {
            hintUsed = true;
            hintButton.setEnabled(false);
            hintButton.setBackgroundColor(Color.GRAY);

            int hintType = random.nextInt(3);
            switch (hintType) {
                case 0:
                    hintTextView.setText("💡 Pista: El tipo es " + targetPokemon.getTypeDisplay());
                    break;
                case 1:
                    hintTextView.setText("💡 Pista: El color es " + targetPokemon.getColor());
                    break;
                case 2:
                    hintTextView.setText("💡 Pista: Fase " + targetPokemon.getGeneration());
                    break;
            }
            hintTextView.setVisibility(View.VISIBLE);
            hintTextView.setAlpha(0f);
            hintTextView.animate().alpha(1f).setDuration(1000).start();
        } else {
            Toast.makeText(this, "Ya usaste tu pista", Toast.LENGTH_SHORT).show();
        }
    }

    private void processGuess() {
        String guess = inputEditText.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(guess)) {
            Toast.makeText(this, "Ingresa un Pokémon, Tipo, Color o Fase", Toast.LENGTH_SHORT).show();
            return;
        }

        GuessResult result = processInput(guess);

        if (result.type.equals("INVÁLIDO")) {
            Toast.makeText(this, "Entrada no válida. Usa Pokémon, Tipo, Color o Fase", Toast.LENGTH_SHORT).show();
            return;
        }

        updateGrid(result);
        currentAttempt++;
        attemptsTextView.setText("Intentos: " + currentAttempt + "/" + MAX_ATTEMPTS);
        inputEditText.setText("");

        if (result.isCorrectPokemon()) {
            showResult(true);
        } else if (currentAttempt >= MAX_ATTEMPTS) {
            showResult(false);
        }
    }

    private GuessResult processInput(String input) {
        GuessResult result = new GuessResult();
        result.input = input;

        // Verificar si es un Pokémon
        Pokemon guessedPokemon = findPokemon(input);
        if (guessedPokemon != null) {
            result.type = "POKÉMON";
            result.pokemonName = guessedPokemon.getName();
            result.pokemonType1 = guessedPokemon.getType1();
            result.pokemonType2 = guessedPokemon.getType2();
            result.pokemonColor = guessedPokemon.getColor();
            result.pokemonPhase = guessedPokemon.getGeneration(); // Cambiado a Fase
            result.isCorrectPokemon = input.equals(targetPokemon.getName());

            result.isCorrectType1 = guessedPokemon.getType1().equals(targetPokemon.getType1());
            result.isCorrectType2 = guessedPokemon.hasTwoTypes() && targetPokemon.hasTwoTypes() &&
                    guessedPokemon.getType2().equals(targetPokemon.getType2());
            result.isCorrectColor = guessedPokemon.getColor().equals(targetPokemon.getColor());
            result.isCorrectPhase = guessedPokemon.getGeneration().equals(targetPokemon.getGeneration()); // Cambiado a Fase
            return result;
        }

        // Verificar si es un tipo
        if (isValidType(input)) {
            result.type = "TIPO";
            result.isCorrectType1 = input.equals(targetPokemon.getType1());
            result.isCorrectType2 = targetPokemon.hasTwoTypes() && input.equals(targetPokemon.getType2());
            result.pokemonType1 = input;
            result.pokemonType2 = "";
            return result;
        }

        // Verificar si es un color
        if (isValidColor(input)) {
            result.type = "COLOR";
            result.isCorrectColor = input.equals(targetPokemon.getColor());
            result.pokemonColor = input;
            return result;
        }

        // Verificar si es una fase
        if (isValidPhase(input)) {
            result.type = "FASE";
            result.isCorrectPhase = input.equals(targetPokemon.getGeneration()); // Cambiado a Fase
            result.pokemonPhase = input;
            return result;
        }

        result.type = "INVÁLIDO";
        return result;
    }

    private Pokemon findPokemon(String name) {
        for (Pokemon pokemon : pokemonList) {
            if (pokemon.getName().equals(name)) {
                return pokemon;
            }
        }
        return null;
    }

    private boolean isValidType(String type) {
        String[] validTypes = {"ELÉCTRICO", "FUEGO", "PLANTA", "AGUA", "NORMAL", "PSÍQUICO",
                "LUCHA", "DRAGÓN", "FANTASMA", "ROCA", "ACERO", "VOLADOR",
                "TIERRA", "SINIESTRO", "HADA", "BICHO", "HIELO", "VENENO"};
        return Arrays.asList(validTypes).contains(type);
    }

    private boolean isValidColor(String color) {
        String[] validColors = {"AMARILLO", "NARANJA", "VERDE", "AZUL", "ROSA", "BLANCO",
                "MARRÓN", "MORADO", "ROJO", "GRIS"};
        return Arrays.asList(validColors).contains(color);
    }

    private boolean isValidPhase(String phase) {
        String[] validPhases = {"BÁSICO", "1", "2"};
        return Arrays.asList(validPhases).contains(phase);
    }

    private void setGridTypeColor(TextView typeTextView, String type) {
        switch (type.toUpperCase()) {
            case "ELÉCTRICO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_electric));
                break;
            case "FUEGO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_fire));
                break;
            case "AGUA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_water));
                break;
            case "PLANTA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_grass));
                break;
            case "NORMAL":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_normal));
                break;
            case "PSÍQUICO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_psychic));
                break;
            case "LUCHA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_fighting));
                break;
            case "DRAGÓN":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_dragon));
                break;
            case "FANTASMA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_ghost));
                break;
            case "ROCA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_rock));
                break;
            case "ACERO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_steel));
                break;
            case "VOLADOR":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_flying));
                break;
            case "TIERRA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_ground));
                break;
            case "SINIESTRO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_dark));
                break;
            case "HADA":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_fairy));
                break;
            case "BICHO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_bug));
                break;
            case "HIELO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_ice));
                break;
            case "VENENO":
                typeTextView.setTextColor(ContextCompat.getColor(this, R.color.type_poison));
                break;
            default:
                typeTextView.setTextColor(Color.WHITE);
                break;
        }
    }

    private void showResult(boolean won) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("WON", won);
        intent.putExtra("POKEMON", targetPokemon.getName());
        intent.putExtra("TYPE", targetPokemon.getTypeDisplay());
        intent.putExtra("COLOR", targetPokemon.getColor());
        intent.putExtra("GENERATION", targetPokemon.getGeneration()); // Se mantiene el nombre pero ahora es Fase
        intent.putExtra("IMAGE_NAME", targetPokemon.getImageName());
        intent.putExtra("ATTEMPTS", currentAttempt);
        startActivity(intent);
        finish();
    }

    // Clase interna para manejar resultados de guesses
    private class GuessResult {
        String input;
        String type;
        String pokemonName;
        String pokemonType1;
        String pokemonType2;
        String pokemonColor;
        String pokemonPhase; // Cambiado de pokemonGeneration a pokemonPhase
        boolean isCorrectPokemon = false;
        boolean isCorrectType1 = false;
        boolean isCorrectType2 = false;
        boolean isCorrectColor = false;
        boolean isCorrectPhase = false; // Cambiado de isCorrectGeneration a isCorrectPhase

        boolean isCorrectPokemon() {
            return isCorrectPokemon;
        }
    }
}