package com.example.interfazprueba;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokemonDetailActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView textNameNumber, textDescription, textHeightWeight, textAbilities, textMoves, textMegaAbilities;
    private ImageView imagePokemon, imageMega;
    private LinearLayout statsContainer, megaStatsContainer, megaContainer, evolutionsContainer, typesContainer;

    private TextToSpeech tts;
    private boolean openedFromScanner = false;
    private String flavorTextForSpeech = "";
    private String pokemonNameForSpeech = "";
    private String preEvolutionName = "";

    // Colores para tipos Pokémon (igual que en PokedexActivity)
    private Map<String, Integer> typeColors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        // Inicializar colores primero
        initializeTypeColors();

        // Luego inicializar vistas
        initializeViews();

        String pokemonName = getIntent().getStringExtra("pokemonName");
        openedFromScanner = getIntent().hasExtra("openedFromScanner");
        pokemonNameForSpeech = pokemonName != null ? pokemonName : "";

        if (pokemonName != null && !pokemonName.isEmpty()) {
            loadPokemonData(pokemonName.toLowerCase());
        } else {
            // Manejar caso donde no hay nombre de Pokémon
            textNameNumber.setText("Pokémon no encontrado");
        }

        if (openedFromScanner) {
            tts = new TextToSpeech(this, this);
        }
    }

    private void initializeTypeColors() {
        typeColors.put("normal", Color.parseColor("#A8A878"));
        typeColors.put("fighting", Color.parseColor("#C03028"));
        typeColors.put("flying", Color.parseColor("#A890F0"));
        typeColors.put("poison", Color.parseColor("#A040A0"));
        typeColors.put("ground", Color.parseColor("#E0C068"));
        typeColors.put("rock", Color.parseColor("#B8A038"));
        typeColors.put("bug", Color.parseColor("#A8B820"));
        typeColors.put("ghost", Color.parseColor("#705898"));
        typeColors.put("steel", Color.parseColor("#B8B8D0"));
        typeColors.put("fire", Color.parseColor("#F08030"));
        typeColors.put("water", Color.parseColor("#6890F0"));
        typeColors.put("grass", Color.parseColor("#78C850"));
        typeColors.put("electric", Color.parseColor("#F8D030"));
        typeColors.put("psychic", Color.parseColor("#F85888"));
        typeColors.put("ice", Color.parseColor("#98D8D8"));
        typeColors.put("dragon", Color.parseColor("#7038F8"));
        typeColors.put("dark", Color.parseColor("#705848"));
        typeColors.put("fairy", Color.parseColor("#EE99AC"));
    }

    private void initializeViews() {
        try {
            ImageButton btnBack = findViewById(R.id.btn_back);
            btnBack.setOnClickListener(v -> finish());

            textNameNumber = findViewById(R.id.textNameNumber);
            imagePokemon = findViewById(R.id.imagePokemon);
            typesContainer = findViewById(R.id.types_container);
            textDescription = findViewById(R.id.textDescription);
            textHeightWeight = findViewById(R.id.textHeightWeight);
            textAbilities = findViewById(R.id.textAbilities);
            statsContainer = findViewById(R.id.statsContainer);
            evolutionsContainer = findViewById(R.id.evolutionsContainer);
            textMoves = findViewById(R.id.textMoves);
            megaContainer = findViewById(R.id.megaContainer);
            imageMega = findViewById(R.id.imageMega);
            textMegaAbilities = findViewById(R.id.textMegaAbilities);
            megaStatsContainer = findViewById(R.id.megaStatsContainer);

            // Inicializar textos por defecto
            textNameNumber.setText("Cargando...");
            textDescription.setText("Cargando descripción...");
            textHeightWeight.setText("Altura: ? m / Peso: ? kg");
            textAbilities.setText("Habilidades: ...");
            textMoves.setText("Movimientos: ...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPokemonData(String name) {
        PokeApiService.getApi().getPokemon(name).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> textNameNumber.setText("Error al cargar Pokémon"));
                    return;
                }

                try {
                    JsonObject p = response.body();

                    // Verificar que los datos esenciales existen
                    if (!p.has("id") || !p.has("name")) {
                        runOnUiThread(() -> textNameNumber.setText("Datos incompletos"));
                        return;
                    }

                    String pokemonName = p.get("name").getAsString();
                    int pokemonId = p.get("id").getAsInt();

                    runOnUiThread(() -> {
                        textNameNumber.setText("#" + pokemonId + " " + capitalizeFirst(pokemonName));
                    });

                    // Cargar imagen
                    if (p.has("sprites")) {
                        JsonObject sprites = p.getAsJsonObject("sprites");
                        String imageUrl = null;
                        if (sprites.has("front_default") && !sprites.get("front_default").isJsonNull()) {
                            imageUrl = sprites.get("front_default").getAsString();
                        }

                        if (imageUrl != null) {
                            String finalImageUrl = imageUrl;
                            runOnUiThread(() -> {
                                Glide.with(PokemonDetailActivity.this)
                                        .load(finalImageUrl)
                                        .into(imagePokemon);
                            });
                        }
                    }

                    // Configurar tipos con badges
                    if (p.has("types")) {
                        runOnUiThread(() -> {
                            typesContainer.removeAllViews();
                        });
                        JsonArray typesArray = p.getAsJsonArray("types");
                        for (JsonElement t : typesArray) {
                            if (t.isJsonObject()) {
                                JsonObject typeObj = t.getAsJsonObject();
                                if (typeObj.has("type")) {
                                    JsonObject typeData = typeObj.getAsJsonObject("type");
                                    if (typeData.has("name")) {
                                        String typeName = typeData.get("name").getAsString();
                                        runOnUiThread(() -> addTypeBadge(typeName));
                                    }
                                }
                            }
                        }
                    }

                    // Altura y peso
                    if (p.has("height") && p.has("weight")) {
                        double height = p.get("height").getAsDouble() / 10.0;
                        double weight = p.get("weight").getAsDouble() / 10.0;
                        runOnUiThread(() -> {
                            textHeightWeight.setText(String.format("Altura: %.1f m / Peso: %.1f kg", height, weight));
                        });
                    }

                    // Habilidades
                    if (p.has("abilities")) {
                        StringBuilder ab = new StringBuilder();
                        JsonArray abilitiesArray = p.getAsJsonArray("abilities");
                        for (JsonElement a : abilitiesArray) {
                            if (a.isJsonObject()) {
                                JsonObject abObj = a.getAsJsonObject();
                                if (abObj.has("ability")) {
                                    JsonObject abilityData = abObj.getAsJsonObject("ability");
                                    if (abilityData.has("name")) {
                                        String nameAb = capitalizeFirst(abilityData.get("name").getAsString());
                                        boolean isHidden = abObj.has("is_hidden") && abObj.get("is_hidden").getAsBoolean();
                                        if (isHidden) nameAb += " (Oculta)";
                                        ab.append(nameAb).append(", ");
                                    }
                                }
                            }
                        }
                        if (ab.length() > 2) {
                            ab.setLength(ab.length() - 2);
                        }
                        String finalAbilities = ab.toString();
                        runOnUiThread(() -> {
                            textAbilities.setText("Habilidades: " + finalAbilities);
                        });
                    }

                    // Estadísticas
                    if (p.has("stats")) {
                        runOnUiThread(() -> {
                            statsContainer.removeAllViews();
                        });
                        JsonArray statsArray = p.getAsJsonArray("stats");
                        int total = 0;
                        for (JsonElement s : statsArray) {
                            if (s.isJsonObject()) {
                                JsonObject stat = s.getAsJsonObject();
                                if (stat.has("base_stat") && stat.has("stat")) {
                                    JsonObject statData = stat.getAsJsonObject("stat");
                                    if (statData.has("name")) {
                                        int value = stat.get("base_stat").getAsInt();
                                        total += value;
                                        String statName = statData.get("name").getAsString();
                                        runOnUiThread(() -> addStatBar(statsContainer, statName, value, false));
                                    }
                                }
                            }
                        }
                        int finalTotal = total;
                        runOnUiThread(() -> addStatBar(statsContainer, "Total", finalTotal, true));
                    }

                    // Movimientos
                    runOnUiThread(() -> showMoves(p));

                    // Cargar información adicional de la especie
                    loadSpeciesAndForms(name);

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> textNameNumber.setText("Error procesando datos"));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                runOnUiThread(() -> textNameNumber.setText("Error de conexión"));
            }
        });
    }

    private void showMoves(JsonObject pokemon) {
        try {
            if (!pokemon.has("moves")) {
                textMoves.setText("Movimientos no disponibles");
                return;
            }

            StringBuilder builder = new StringBuilder();
            JsonArray moves = pokemon.getAsJsonArray("moves");
            String[] methods = {"level-up", "machine", "tutor", "egg"};
            String[] titles = {"Por nivel:\n", "\nPor MT/MO:\n", "\nPor tutor:\n", "\nPor huevo:\n"};

            for (int i = 0; i < methods.length; i++) {
                builder.append(titles[i]);
                boolean hasMovesInCategory = false;

                for (JsonElement e : moves) {
                    if (e.isJsonObject()) {
                        JsonObject m = e.getAsJsonObject();
                        if (m.has("move") && m.has("version_group_details")) {
                            String name = capitalizeFirst(m.getAsJsonObject("move").get("name").getAsString());
                            JsonArray details = m.getAsJsonArray("version_group_details");

                            for (JsonElement d : details) {
                                if (d.isJsonObject()) {
                                    JsonObject detail = d.getAsJsonObject();
                                    if (detail.has("move_learn_method")) {
                                        JsonObject method = detail.getAsJsonObject("move_learn_method");
                                        if (method.has("name") && method.get("name").getAsString().equals(methods[i])) {
                                            builder.append(" - ").append(name).append("\n");
                                            hasMovesInCategory = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!hasMovesInCategory) {
                    builder.append(" - Ninguno\n");
                }
            }

            textMoves.setText(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            textMoves.setText("Error cargando movimientos");
        }
    }

    private void loadSpeciesAndForms(String name) {
        PokeApiService.getApi().getPokemonSpecies(name).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                try {
                    JsonObject species = response.body();

                    // Descripción
                    String flavorText = "Descripción no disponible";
                    if (species.has("flavor_text_entries")) {
                        JsonArray flavorEntries = species.getAsJsonArray("flavor_text_entries");
                        for (JsonElement e : flavorEntries) {
                            if (e.isJsonObject()) {
                                JsonObject entry = e.getAsJsonObject();
                                if (entry.has("language") && entry.has("flavor_text")) {
                                    JsonObject language = entry.getAsJsonObject("language");
                                    if (language.has("name") && language.get("name").getAsString().equals("es")) {
                                        flavorText = entry.get("flavor_text").getAsString()
                                                .replace("\n", " ").replace("\f", " ");
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    String finalFlavorText = flavorText;
                    runOnUiThread(() -> textDescription.setText(finalFlavorText));

                    // Obtener preevolución si existe
                    if (species.has("evolves_from_species") && !species.get("evolves_from_species").isJsonNull()) {
                        preEvolutionName = species.getAsJsonObject("evolves_from_species").get("name").getAsString();
                    } else {
                        preEvolutionName = "";
                    }

                    // Texto que se leerá dinámicamente
                    if (!preEvolutionName.isEmpty()) {
                        flavorTextForSpeech = "Has escaneado a " + pokemonNameForSpeech +
                                ", la forma evolucionada de " + preEvolutionName + ". " + finalFlavorText;
                    } else {
                        flavorTextForSpeech = "Has escaneado a " + pokemonNameForSpeech + ". " + finalFlavorText;
                    }

                    // Leer automáticamente si viene del escáner
                    if (openedFromScanner && tts != null && !flavorTextForSpeech.equals("Descripción no disponible")) {
                        speak(flavorTextForSpeech);
                    }

                    // Cargar evoluciones
                    if (species.has("evolution_chain")) {
                        JsonObject evolutionChain = species.getAsJsonObject("evolution_chain");
                        if (evolutionChain.has("url")) {
                            loadEvolutionChain(evolutionChain.get("url").getAsString());
                        }
                    }

                    // Cargar Megas
                    if (species.has("varieties")) {
                        JsonArray varieties = species.getAsJsonArray("varieties");
                        for (JsonElement v : varieties) {
                            if (v.isJsonObject()) {
                                JsonObject variety = v.getAsJsonObject();
                                if (variety.has("pokemon")) {
                                    JsonObject pokemonVariety = variety.getAsJsonObject("pokemon");
                                    if (pokemonVariety.has("name")) {
                                        String varName = pokemonVariety.get("name").getAsString();
                                        if (!varName.equals(name) && varName.toLowerCase().contains("mega")) {
                                            PokeApiService.getApi().getPokemon(varName).enqueue(new Callback<JsonObject>() {
                                                @Override
                                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        showMegaData(response.body());
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    // Método para agregar badges de tipos
    private void addTypeBadge(String type) {
        try {
            TextView typeView = new TextView(this);
            typeView.setText(type.toUpperCase());
            typeView.setTextColor(Color.WHITE);
            typeView.setTextSize(12f);
            typeView.setPadding(20, 10, 20, 10);
            typeView.setTypeface(null, Typeface.BOLD);

            GradientDrawable typeBackground = new GradientDrawable();
            typeBackground.setShape(GradientDrawable.RECTANGLE);
            typeBackground.setCornerRadius(16f);

            Integer typeColor = typeColors.get(type.toLowerCase());
            typeBackground.setColor(typeColor != null ? typeColor : Color.LTGRAY);

            typeView.setBackground(typeBackground);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            typeView.setLayoutParams(params);

            typesContainer.addView(typeView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addStatBar(LinearLayout container, String name, int value, boolean isTotal) {
        try {
            LinearLayout barLayout = new LinearLayout(this);
            barLayout.setOrientation(LinearLayout.HORIZONTAL);
            barLayout.setPadding(0, 8, 0, 8);
            barLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView label = new TextView(this);
            label.setText(capitalizeFirst(name.replace("-", " ")) + ": " + value);
            label.setTextColor(Color.parseColor("#D32F2F"));
            label.setTextSize(14f);
            label.setTypeface(null, Typeface.BOLD);
            label.setWidth(180);

            View bar = new View(this);
            int barWidth = Math.min(value * 2, 300);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, 20);
            barParams.setMargins(16, 0, 0, 0);
            bar.setLayoutParams(barParams);

            GradientDrawable barBackground = new GradientDrawable();
            barBackground.setShape(GradientDrawable.RECTANGLE);
            barBackground.setCornerRadius(10f);
            barBackground.setColor(isTotal ? Color.parseColor("#757575") : Color.parseColor("#4CAF50"));
            bar.setBackground(barBackground);

            barLayout.addView(label);
            barLayout.addView(bar);
            container.addView(barLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMegaData(JsonObject mega) {
        try {
            runOnUiThread(() -> {
                megaContainer.setVisibility(View.VISIBLE);

                if (mega.has("sprites")) {
                    JsonObject sprites = mega.getAsJsonObject("sprites");
                    if (sprites.has("front_default") && !sprites.get("front_default").isJsonNull()) {
                        String imageUrl = sprites.get("front_default").getAsString();
                        Glide.with(PokemonDetailActivity.this)
                                .load(imageUrl)
                                .into(imageMega);
                    }
                }

                megaStatsContainer.removeAllViews();
                if (mega.has("stats")) {
                    JsonArray statsArray = mega.getAsJsonArray("stats");
                    for (JsonElement s : statsArray) {
                        if (s.isJsonObject()) {
                            JsonObject stat = s.getAsJsonObject();
                            if (stat.has("base_stat") && stat.has("stat")) {
                                JsonObject statData = stat.getAsJsonObject("stat");
                                if (statData.has("name")) {
                                    int value = stat.get("base_stat").getAsInt();
                                    String statName = statData.get("name").getAsString();
                                    addStatBar(megaStatsContainer, statName, value, false);
                                }
                            }
                        }
                    }
                }

                if (mega.has("abilities")) {
                    StringBuilder ab = new StringBuilder();
                    JsonArray abilitiesArray = mega.getAsJsonArray("abilities");
                    for (JsonElement a : abilitiesArray) {
                        if (a.isJsonObject()) {
                            JsonObject abObj = a.getAsJsonObject();
                            if (abObj.has("ability")) {
                                JsonObject abilityData = abObj.getAsJsonObject("ability");
                                if (abilityData.has("name")) {
                                    String abilityName = capitalizeFirst(abilityData.get("name").getAsString());
                                    boolean isHidden = abObj.has("is_hidden") && abObj.get("is_hidden").getAsBoolean();
                                    if (isHidden) abilityName += " (Oculta)";
                                    ab.append(abilityName).append(", ");
                                }
                            }
                        }
                    }
                    if (ab.length() > 2) ab.setLength(ab.length() - 2);
                    textMegaAbilities.setText("Habilidades: " + ab.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadEvolutionChain(String url) {
        PokeApiService.getApi().getEvolutionChain(url).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                try {
                    JsonObject evolutionChain = response.body();
                    evolutionsContainer.removeAllViews();

                    List<String> evolutionNames = new ArrayList<>();
                    collectEvolutionNames(evolutionChain.getAsJsonObject("chain"), evolutionNames);

                    List<JsonObject> evolutionData = new ArrayList<>();
                    for (String name : evolutionNames) {
                        PokeApiService.getApi().getPokemon(name).enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    evolutionData.add(response.body());
                                    if (evolutionData.size() == evolutionNames.size()) {
                                        evolutionData.sort((a, b) ->
                                                Integer.compare(a.get("id").getAsInt(), b.get("id").getAsInt()));
                                        runOnUiThread(() -> showEvolutionImages(evolutionData));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    private void collectEvolutionNames(JsonObject chain, List<String> list) {
        try {
            if (chain.has("species") && chain.getAsJsonObject("species").has("name")) {
                String name = chain.getAsJsonObject("species").get("name").getAsString();
                list.add(name);
            }
            if (chain.has("evolves_to")) {
                JsonArray evolvesTo = chain.getAsJsonArray("evolves_to");
                for (JsonElement e : evolvesTo) {
                    if (e.isJsonObject()) {
                        collectEvolutionNames(e.getAsJsonObject(), list);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEvolutionImages(List<JsonObject> pokemons) {
        try {
            runOnUiThread(() -> {
                evolutionsContainer.removeAllViews();
                for (JsonObject p : pokemons) {
                    if (p.has("sprites")) {
                        JsonObject sprites = p.getAsJsonObject("sprites");
                        if (sprites.has("front_default") && !sprites.get("front_default").isJsonNull()) {
                            ImageView evoImage = new ImageView(PokemonDetailActivity.this);
                            int sizeInDp = 100;
                            float scale = getResources().getDisplayMetrics().density;
                            int sizeInPx = (int) (sizeInDp * scale + 0.5f);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                            params.setMargins(8, 0, 8, 0);
                            evoImage.setLayoutParams(params);

                            String imageUrl = sprites.get("front_default").getAsString();
                            Glide.with(PokemonDetailActivity.this)
                                    .load(imageUrl)
                                    .into(evoImage);

                            evolutionsContainer.addView(evoImage);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- TextToSpeech ---
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("es", "ES"));
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setPitch(1.4f);
                tts.setSpeechRate(1);
            }

            try {
                for (Voice v : tts.getVoices()) {
                    if (v.getName().toLowerCase().contains("x-robot") ||
                            v.getName().toLowerCase().contains("rmk") ||
                            v.getName().toLowerCase().contains("es")) {
                        tts.setVoice(v);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (openedFromScanner && !flavorTextForSpeech.isEmpty()) {
                speak(flavorTextForSpeech);
            }
        }
    }

    private void speak(String text) {
        if (tts != null) {
            tts.stop();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}