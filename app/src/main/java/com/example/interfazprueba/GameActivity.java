package com.example.interfazprueba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
    private Button timerModeButton;
    private TextView attemptsTextView;
    private TextView hintTextView;
    private TextView timerTextView;
    private Button backButton;
    private ImageView topPokemonImage;
    private ImageView bottomPokemonImage;
    private LinearLayout gridContainer;

    private Pokemon targetPokemon;
    private List<Pokemon> pokemonList;
    private int currentAttempt = 0;
    private final int MAX_ATTEMPTS = 10;
    private List<TextView[]> guessRows;
    private boolean hintUsed = false;

    private Handler imageHandler = new Handler();
    private Random random = new Random();

    // Variables para modo contrareloj
    private CountDownTimer gameTimer;
    private final long TOTAL_GAME_TIME = 120000; // 2 minutos en milisegundos
    private boolean isTimerMode = false;
    private long timeLeftInMillis = TOTAL_GAME_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializePokemonList();
        selectRandomPokemon();
        setupUI();
        setupGrid();
        startImageRotation();
    }

    private void toggleTimerMode() {
        if (isTimerMode) {
            // Desactivar modo contrareloj
            stopTimerMode();
            timerModeButton.setText("⏰ Modo Contrareloj");
            Toast.makeText(this, "Modo normal activado", Toast.LENGTH_SHORT).show();
        } else {
            // Activar modo contrareloj
            startTimerMode();
            timerModeButton.setText("🔁 Modo Normal");
            Toast.makeText(this, "¡Modo contrareloj activado! 2 minutos", Toast.LENGTH_LONG).show();
        }
        isTimerMode = !isTimerMode;
    }

    private void startTimerMode() {
        // Cambiar fondo a azul
        gridContainer.setBackgroundResource(R.drawable.card_blue_rounded);

        // Mostrar temporizador y ocultar contador de intentos
        timerTextView.setVisibility(View.VISIBLE);
        attemptsTextView.setVisibility(View.GONE);

        // Desactivar pistas en modo contrareloj
        hintButton.setEnabled(false);
        hintButton.setBackgroundColor(Color.GRAY);

        // Cambiar texto del botón
        timerModeButton.setText("🔁 Modo Normal");

        // Cambiar color del botón a rojo cuando está activo el modo contrareloj
        timerModeButton.setBackgroundResource(R.drawable.button_pokemon_red);

        // Reiniciar intentos para modo contrareloj
        currentAttempt = 0;

        // Iniciar temporizador
        gameTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateTimerDisplay();
                // Tiempo agotado
                showResult(false, "¡Tiempo agotado!");
            }
        }.start();
    }

    private void stopTimerMode() {
        // Cambiar fondo a rojo (normal)
        gridContainer.setBackgroundResource(R.drawable.card_red_rounded);

        // Ocultar temporizador y mostrar contador de intentos
        timerTextView.setVisibility(View.GONE);
        attemptsTextView.setVisibility(View.VISIBLE);
        attemptsTextView.setText("Intentos: " + currentAttempt + "/" + MAX_ATTEMPTS);

        // Reactivar pistas
        hintButton.setEnabled(true);
        hintButton.setBackgroundResource(R.drawable.button_pokemon_blue);

        // Cambiar texto del botón
        timerModeButton.setText("⏰ Modo Contrareloj");

        // Detener temporizador
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }

        // Reiniciar tiempo
        timeLeftInMillis = TOTAL_GAME_TIME;

        // Restaurar color del botón de modo contrareloj
        timerModeButton.setBackgroundResource(R.drawable.button_pokemon_blue);
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);

        // Cambiar color cuando quede poco tiempo
        if (timeLeftInMillis < 30000) { // Menos de 30 segundos
            timerTextView.setTextColor(Color.RED);
            timerTextView.setBackgroundColor(Color.YELLOW);
        } else if (timeLeftInMillis < 60000) { // Menos de 1 minuto
            timerTextView.setTextColor(Color.YELLOW);
            timerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.pokemon_red));
        } else {
            timerTextView.setTextColor(Color.WHITE);
            timerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.pokemon_red));
        }
    }

    private void startImageRotation() {
        imageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePokemonImages();
                imageHandler.postDelayed(this, 4000); // Cambiar imágenes cada 4 segundos
            }
        }, 1000);
    }

    private void updatePokemonImages() {
        if (topPokemonImage == null || bottomPokemonImage == null) return;

        // Seleccionar dos Pokémon diferentes
        Pokemon topPokemon = getRandomPokemon();
        Pokemon bottomPokemon = getRandomPokemon();

        // Asegurarse de que sean diferentes
        while (bottomPokemon.getName().equals(topPokemon.getName())) {
            bottomPokemon = getRandomPokemon();
        }

        // Cargar imagen superior
        int topResourceId = getResources().getIdentifier(topPokemon.getImageName(), "drawable", getPackageName());
        if (topResourceId != 0) {
            topPokemonImage.setImageResource(topResourceId);
        }

        // Cargar imagen inferior
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
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }

    private void initializePokemonList() {
        pokemonList = Arrays.asList(
                new Pokemon("PIKACHU", "ELÉCTRICO", "AMARILLO", "1", "pikachu"),
                new Pokemon("BULBASAUR", "PLANTA", "VERDE", "1", "bulbasaur"),
                new Pokemon("SQUIRTLE", "AGUA", "AZUL", "1", "squirtle"),
                new Pokemon("JIGGLYPUFF", "HADA", "ROSA", "1", "jigglypuff"),
                new Pokemon("MEWTWO", "PSÍQUICO", "BLANCO", "1", "mewtwo"),
                new Pokemon("EEVEE", "NORMAL", "MARRÓN", "1", "eevee"),
                new Pokemon("SNORLAX", "NORMAL", "AZUL", "1", "snorlax"),
                new Pokemon("LUCARIO", "LUCHA", "AZUL", "4", "lucario"),
                new Pokemon("GRENINJA", "AGUA", "AZUL", "6", "greninja"),
                new Pokemon("LUGIA", "PSÍQUICO", "BLANCO", "2", "lugia"),
                new Pokemon("GYARADOS", "AGUA", "AZUL", "1", "gyarados"),
                new Pokemon("GENGAR", "FANTASMA", "MORADO", "1", "gengar"),
                new Pokemon("ALAKAZAM", "PSÍQUICO", "MARRÓN", "1", "alakazam"),
                new Pokemon("ARCANINE", "FUEGO", "NARANJA", "1", "arcanine"),
                new Pokemon("LAPRAS", "AGUA", "AZUL", "1", "lapras"),
                new Pokemon("CHARIZARD", "FUEGO", "VOLADOR", "NARANJA", "1", "charizard"),
                new Pokemon("GARCHOMP", "DRAGÓN", "TIERRA", "AZUL", "4", "garchomp"),
                new Pokemon("DRAGONITE", "DRAGÓN", "VOLADOR", "NARANJA", "1", "dragonite"),
                new Pokemon("TYRANITAR", "ROCA", "SINIESTRO", "VERDE", "2", "tyranitar"),
                new Pokemon("METAGROSS", "ACERO", "PSÍQUICO", "AZUL", "3", "metagross")
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
        timerModeButton = findViewById(R.id.timerModeButton);
        attemptsTextView = findViewById(R.id.attemptsTextView);
        hintTextView = findViewById(R.id.hintTextView);
        timerTextView = findViewById(R.id.timerTextView);
        backButton = findViewById(R.id.backButton);
        topPokemonImage = findViewById(R.id.topPokemonImage);
        bottomPokemonImage = findViewById(R.id.bottomPokemonImage);
        gridContainer = findViewById(R.id.gridContainer);

        // Botón para volver al menú principal
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameTimer != null) {
                    gameTimer.cancel();
                }
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Botón para alternar modo contrareloj
        timerModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimerMode();
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
        inputEditText.setHint("Ej: PIKACHU, FUEGO, AZUL, 1");

        // Mostrar primeras imágenes
        updatePokemonImages();
    }

    private void showHint() {
        if (!hintUsed && !isTimerMode) {
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
                    hintTextView.setText("💡 Pista: Generación " + targetPokemon.getGeneration());
                    break;
            }
            hintTextView.setVisibility(View.VISIBLE);

            hintTextView.setAlpha(0f);
            hintTextView.animate().alpha(1f).setDuration(1000).start();
        } else if (isTimerMode) {
            Toast.makeText(this, "Pistas no disponibles en modo contrareloj", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ya usaste tu pista", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGrid() {
        guessRows = new ArrayList<>();
        gridLayout.removeAllViews();

        // Configurar encabezados - CON FONDO ROJO
        String[] headerTexts = {"NOMBRE", "TIPO 1", "TIPO 2", "COLOR", "GEN"};
        for (int col = 0; col < 5; col++) {
            TextView header = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(0);
            params.columnSpec = GridLayout.spec(col);
            params.width = 160; // Ancho fijo para mejor alineación
            params.height = 55; // Alto fijo
            params.setMargins(2, 2, 2, 2);

            header.setLayoutParams(params);
            header.setTextSize(12);
            header.setBackgroundColor(ContextCompat.getColor(this, R.color.pokemon_red));
            header.setTextColor(ContextCompat.getColor(this, R.color.pokemon_yellow));
            header.setGravity(android.view.Gravity.CENTER);
            header.setPadding(4, 8, 4, 8);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            header.setShadowLayer(1, 1, 1, Color.BLACK);
            header.setMaxLines(1);
            header.setEllipsize(TextUtils.TruncateAt.END);
            header.setText(headerTexts[col]);

            gridLayout.addView(header);
        }

        // Configurar filas de guesses
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            TextView[] rowViews = new TextView[5];
            for (int col = 0; col < 5; col++) {
                TextView textView = new TextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row + 1);
                params.columnSpec = GridLayout.spec(col);
                params.width = 160; // Ancho fijo
                params.height = 55; // Alto fijo
                params.setMargins(2, 2, 2, 2);

                textView.setLayoutParams(params);
                textView.setTextSize(10);
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setPadding(3, 3, 3, 3);
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
                textView.setMaxLines(1);
                textView.setEllipsize(TextUtils.TruncateAt.END);

                gridLayout.addView(textView);
                rowViews[col] = textView;
            }
            guessRows.add(rowViews);
        }
    }

    private void processGuess() {
        String guess = inputEditText.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(guess)) {
            Toast.makeText(this, "Ingresa un Pokémon, Tipo, Color o Generación", Toast.LENGTH_SHORT).show();
            return;
        }

        GuessResult result = processInput(guess);

        if (result.type.equals("INVÁLIDO")) {
            Toast.makeText(this, "Entrada no válida. Usa Pokémon, Tipo, Color o Gen", Toast.LENGTH_SHORT).show();
            return;
        }

        updateGrid(result);

        if (!isTimerMode) {
            currentAttempt++;
            attemptsTextView.setText("Intentos: " + currentAttempt + "/" + MAX_ATTEMPTS);
        }

        inputEditText.setText("");

        if (result.isCorrectPokemon()) {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            String message = isTimerMode ?
                    "¡Adivinaste con " + getTimeLeftString() + " restantes!" :
                    "¡Adivinaste en " + currentAttempt + " intentos!";
            showResult(true, message);
        } else if (!isTimerMode && currentAttempt >= MAX_ATTEMPTS) {
            showResult(false, "¡Se acabaron los intentos!");
        }
    }

    private String getTimeLeftString() {
        int seconds = (int) (timeLeftInMillis / 1000);
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    // ... (el resto de los métodos processInput, findPokemon, isValidType, etc. se mantienen igual)

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
            result.pokemonGeneration = "Gen " + guessedPokemon.getGeneration();
            result.isCorrectPokemon = input.equals(targetPokemon.getName());

            // Verificar tipos - ahora separados
            result.isCorrectType1 = guessedPokemon.getType1().equals(targetPokemon.getType1());
            result.isCorrectType2 = guessedPokemon.hasTwoTypes() && targetPokemon.hasTwoTypes() &&
                    guessedPokemon.getType2().equals(targetPokemon.getType2());

            result.isCorrectColor = guessedPokemon.getColor().equals(targetPokemon.getColor());
            result.isCorrectGeneration = guessedPokemon.getGeneration().equals(targetPokemon.getGeneration());
            return result;
        }

        // Verificar si es un tipo (ahora funciona con cualquiera de los dos tipos del Pokémon objetivo)
        if (isValidType(input)) {
            result.type = "TIPO";
            result.isCorrectType1 = input.equals(targetPokemon.getType1());
            result.isCorrectType2 = targetPokemon.hasTwoTypes() && input.equals(targetPokemon.getType2());
            result.pokemonType1 = input;
            result.pokemonType2 = ""; // Vacío para tipo individual
            return result;
        }

        // Verificar si es un color
        if (isValidColor(input)) {
            result.type = "COLOR";
            result.isCorrectColor = input.equals(targetPokemon.getColor());
            result.pokemonColor = input;
            return result;
        }

        // Verificar si es una generación
        if (isValidGeneration(input)) {
            result.type = "GENERACIÓN";
            result.isCorrectGeneration = input.equals(targetPokemon.getGeneration());
            result.pokemonGeneration = "Gen " + input;
            return result;
        }

        // Si no es válido
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
                "MARRÓN", "MORADO"};
        return Arrays.asList(validColors).contains(color);
    }

    private boolean isValidGeneration(String generation) {
        String[] validGenerations = {"1", "2", "3", "4", "5", "6"};
        return Arrays.asList(validGenerations).contains(generation);
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

        // Generación
        if (result.pokemonGeneration != null) {
            currentRow[4].setText(result.pokemonGeneration);
            if (result.isCorrectGeneration) {
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

        // Animación simple al actualizar
        for (TextView tv : currentRow) {
            tv.setScaleX(0.8f);
            tv.setScaleY(0.8f);
            tv.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
        }
    }

    // Método auxiliar para colores en el grid
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
            default:
                typeTextView.setTextColor(Color.WHITE);
                break;
        }
    }

    private void showResult(boolean won, String message) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("WON", won);
        intent.putExtra("POKEMON", targetPokemon.getName());
        intent.putExtra("TYPE", targetPokemon.getTypeDisplay());
        intent.putExtra("COLOR", targetPokemon.getColor());
        intent.putExtra("GENERATION", targetPokemon.getGeneration());
        intent.putExtra("IMAGE_NAME", targetPokemon.getImageName());
        intent.putExtra("ATTEMPTS", currentAttempt);
        intent.putExtra("MESSAGE", message);
        intent.putExtra("TIMER_MODE", isTimerMode);
        if (isTimerMode) {
            intent.putExtra("TIME_LEFT", timeLeftInMillis);
        }
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
        String pokemonGeneration;
        boolean isCorrectPokemon = false;
        boolean isCorrectType1 = false;
        boolean isCorrectType2 = false;
        boolean isCorrectColor = false;
        boolean isCorrectGeneration = false;

        boolean isCorrectPokemon() {
            return isCorrectPokemon;
        }
    }
}
