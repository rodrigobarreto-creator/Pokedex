package com.example.interfazprueba;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokemonDetailActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView textNameNumber, textTypes, textDescription, textHeightWeight, textAbilities,
            textEvolutions, textMoves, textMegaAbilities;
    private ImageView imagePokemon, imageMega;
    private LinearLayout statsContainer, megaStatsContainer, megaContainer, evolutionsContainer;

    private TextToSpeech tts;
    private boolean openedFromScanner = false;
    private String flavorTextForSpeech = "";
    private String pokemonNameForSpeech = "";
    private String preEvolutionName = ""; // ← NUEVO: para guardar la preevolución

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        textNameNumber = findViewById(R.id.textNameNumber);
        imagePokemon = findViewById(R.id.imagePokemon);
        textTypes = findViewById(R.id.textTypes);
        textDescription = findViewById(R.id.textDescription);
        textHeightWeight = findViewById(R.id.textHeightWeight);
        textAbilities = findViewById(R.id.textAbilities);
        statsContainer = findViewById(R.id.statsContainer);
        textEvolutions = findViewById(R.id.textEvolutions);
        evolutionsContainer = findViewById(R.id.evolutionsContainer);
        textMoves = findViewById(R.id.textMoves);
        megaContainer = findViewById(R.id.megaContainer);
        imageMega = findViewById(R.id.imageMega);
        textMegaAbilities = findViewById(R.id.textMegaAbilities);
        megaStatsContainer = findViewById(R.id.megaStatsContainer);

        String pokemonName = getIntent().getStringExtra("pokemonName");
        openedFromScanner = getIntent().hasExtra("openedFromScanner");
        pokemonNameForSpeech = pokemonName != null ? pokemonName : "";

        if (pokemonName != null) {
            loadPokemonData(pokemonName.toLowerCase());
        }

        if (openedFromScanner) {
            tts = new TextToSpeech(this, this);
        }
    }

    private void loadPokemonData(String name) {
        PokeApiService.getApi().getPokemon(name).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                JsonObject p = response.body();

                textNameNumber.setText("#" + p.get("id").getAsInt() + " " + p.get("name").getAsString());

                Glide.with(PokemonDetailActivity.this)
                        .load(p.getAsJsonObject("sprites").get("front_default").getAsString())
                        .into(imagePokemon);

                StringBuilder types = new StringBuilder();
                for (JsonElement t : p.getAsJsonArray("types")) {
                    types.append(t.getAsJsonObject().getAsJsonObject("type").get("name").getAsString()).append(", ");
                }
                if (types.length() > 2) types.setLength(types.length() - 2);
                textTypes.setText("Tipos: " + types);

                textHeightWeight.setText("Altura: " + (p.get("height").getAsDouble() / 10) +
                        " m / Peso: " + (p.get("weight").getAsDouble() / 10) + " kg");

                StringBuilder ab = new StringBuilder();
                for (JsonElement a : p.getAsJsonArray("abilities")) {
                    JsonObject abObj = a.getAsJsonObject();
                    String nameAb = abObj.getAsJsonObject("ability").get("name").getAsString();
                    if (abObj.get("is_hidden").getAsBoolean()) nameAb += " (Oculta)";
                    ab.append(nameAb).append(", ");
                }
                if (ab.length() > 2) ab.setLength(ab.length() - 2);
                textAbilities.setText("Habilidades: " + ab);

                statsContainer.removeAllViews();
                int total = 0;
                for (JsonElement s : p.getAsJsonArray("stats")) {
                    JsonObject stat = s.getAsJsonObject();
                    int value = stat.get("base_stat").getAsInt();
                    total += value;
                    addStatBar(statsContainer, stat.getAsJsonObject("stat").get("name").getAsString(), value);
                }
                addStatBar(statsContainer, "Total", total, true);

                showMoves(p);
                loadSpeciesAndForms(name);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                textNameNumber.setText("Error al cargar Pokémon");
            }
        });
    }

    private void showMoves(JsonObject pokemon) {
        StringBuilder builder = new StringBuilder();
        JsonArray moves = pokemon.getAsJsonArray("moves");
        String[] methods = {"level-up", "machine", "tutor", "egg"};
        String[] titles = {"Por nivel:\n", "\nPor MT/MO:\n", "\nPor tutor:\n", "\nPor huevo:\n"};

        for (int i = 0; i < methods.length; i++) {
            builder.append(titles[i]);
            for (JsonElement e : moves) {
                JsonObject m = e.getAsJsonObject();
                String name = m.getAsJsonObject("move").get("name").getAsString();
                for (JsonElement d : m.getAsJsonArray("version_group_details")) {
                    if (d.getAsJsonObject().getAsJsonObject("move_learn_method").get("name").getAsString().equals(methods[i])) {
                        builder.append(" - ").append(name).append("\n");
                        break;
                    }
                }
            }
        }
        textMoves.setText(builder.toString());
    }

    private void loadSpeciesAndForms(String name) {
        PokeApiService.getApi().getPokemonSpecies(name).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                JsonObject species = response.body();

                // Descripción
                String flavorText = "Descripción no disponible";
                for (JsonElement e : species.getAsJsonArray("flavor_text_entries")) {
                    JsonObject entry = e.getAsJsonObject();
                    if (entry.getAsJsonObject("language").get("name").getAsString().equals("es")) {
                        flavorText = entry.get("flavor_text").getAsString().replace("\n", " ").replace("\f", " ");
                        break;
                    }
                }
                textDescription.setText(flavorText);

                // --- NUEVO: obtener preevolución si existe ---
                if (species.has("evolves_from_species") && !species.get("evolves_from_species").isJsonNull()) {
                    preEvolutionName = species.getAsJsonObject("evolves_from_species").get("name").getAsString();
                } else {
                    preEvolutionName = "";
                }

                // --- Texto que se leerá dinámicamente ---
                if (!preEvolutionName.isEmpty()) {
                    flavorTextForSpeech = "Has escaneado a " + pokemonNameForSpeech +
                            ", la forma evolucionada de " + preEvolutionName + ". " + flavorText;
                } else {
                    flavorTextForSpeech = "Has escaneado a " + pokemonNameForSpeech + ". " + flavorText;
                }

                // --- Leer automáticamente si viene del escáner ---
                if (openedFromScanner && tts != null && !flavorTextForSpeech.equals("Descripción no disponible")) {
                    speak(flavorTextForSpeech);
                }

                // Cargar evoluciones
                if (species.has("evolution_chain")) {
                    loadEvolutionChain(species.getAsJsonObject("evolution_chain").get("url").getAsString());
                }

                // Cargar Megas
                for (JsonElement v : species.getAsJsonArray("varieties")) {
                    String varName = v.getAsJsonObject().getAsJsonObject("pokemon").get("name").getAsString();
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

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    // --- TextToSpeech ---
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("es", "ES"));
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {

                // VOZ ESTILO ROBOT
                tts.setPitch(1.4f);       // tono más agudo (metálico)
                tts.setSpeechRate(1);  // más rápida
            }

            // Si querés forzar una voz sintética
            for (Voice v : tts.getVoices()) {
                if (v.getName().toLowerCase().contains("x-robot") ||
                        v.getName().toLowerCase().contains("rmk") ||
                        v.getName().toLowerCase().contains("es")) {
                    tts.setVoice(v);
                    break;
                }
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

    private void showMegaData(JsonObject mega) {
        megaContainer.setVisibility(android.view.View.VISIBLE);
        Glide.with(this)
                .load(mega.getAsJsonObject("sprites").get("front_default").getAsString())
                .into(imageMega);

        megaStatsContainer.removeAllViews();
        for (JsonElement s : mega.getAsJsonArray("stats")) {
            JsonObject stat = s.getAsJsonObject();
            addStatBar(megaStatsContainer, stat.getAsJsonObject("stat").get("name").getAsString(), stat.get("base_stat").getAsInt());
        }

        StringBuilder ab = new StringBuilder();
        for (JsonElement a : mega.getAsJsonArray("abilities")) {
            JsonObject abObj = a.getAsJsonObject();
            String abilityName = abObj.getAsJsonObject("ability").get("name").getAsString();
            if (abObj.get("is_hidden").getAsBoolean()) abilityName += " (Oculta)";
            ab.append(abilityName).append(", ");
        }
        if (ab.length() > 2) ab.setLength(ab.length() - 2);
        textMegaAbilities.setText("Habilidades: " + ab);
    }

    private void loadEvolutionChain(String url) {
        PokeApiService.getApi().getEvolutionChain(url).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                evolutionsContainer.removeAllViews();

                java.util.List<String> evolutionNames = new java.util.ArrayList<>();
                collectEvolutionNames(response.body().getAsJsonObject("chain"), evolutionNames);

                java.util.List<JsonObject> evolutionData = new java.util.ArrayList<>();
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
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    private void collectEvolutionNames(JsonObject chain, java.util.List<String> list) {
        String name = chain.getAsJsonObject("species").get("name").getAsString();
        list.add(name);
        if (chain.has("evolves_to")) {
            for (JsonElement e : chain.getAsJsonArray("evolves_to")) {
                collectEvolutionNames(e.getAsJsonObject(), list);
            }
        }
    }

    private void showEvolutionImages(java.util.List<JsonObject> pokemons) {
        evolutionsContainer.removeAllViews();
        for (JsonObject p : pokemons) {
            ImageView evoImage = new ImageView(PokemonDetailActivity.this);
            int sizeInDp = 100;
            float scale = getResources().getDisplayMetrics().density;
            int sizeInPx = (int) (sizeInDp * scale + 0.5f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            params.setMargins(8, 0, 8, 0);
            evoImage.setLayoutParams(params);

            Glide.with(PokemonDetailActivity.this)
                    .load(p.getAsJsonObject("sprites").get("front_default").getAsString())
                    .into(evoImage);

            evolutionsContainer.addView(evoImage);
        }
    }

    private void addStatBar(LinearLayout container, String name, int value) {
        addStatBar(container, name, value, false);
    }

    private void addStatBar(LinearLayout container, String name, int value, boolean isTotal) {
        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.HORIZONTAL);
        barLayout.setPadding(0, 4, 0, 4);

        TextView label = new TextView(this);
        label.setText(name + ": " + value);
        label.setTextColor(Color.WHITE);
        label.setWidth(250);

        android.view.View bar = new android.view.View(this);
        int barWidth = Math.min(value * 2, 400);
        bar.setLayoutParams(new LinearLayout.LayoutParams(barWidth, 20));
        bar.setBackgroundColor(isTotal ? Color.GRAY : Color.GREEN);

        barLayout.addView(label);
        barLayout.addView(bar);
        container.addView(barLayout);
    }
}
