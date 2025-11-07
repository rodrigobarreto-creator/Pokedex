package com.example.interfazprueba;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.interfazprueba.scanner.ScannerActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokedexActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PokemonAdapterImproved adapter;
    private List<PokemonFull> pokemonList = new ArrayList<>();
    private List<PokemonFull> displayedList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgress;
    private TextView pokemonCount;
    private TextView filterBadge;

    private final String[] types = {"normal", "fighting", "flying", "poison", "ground",
            "rock", "bug", "ghost", "steel", "fire", "water", "grass",
            "electric", "psychic", "ice", "dragon", "dark", "fairy"};

    private Set<String> selectedTypes = new HashSet<>();
    private boolean isLoading = false;
    private int loadedCount = 0;
    private final int TOTAL_POKEMON = 151;

    private Map<String, Integer> typeColors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        initializeTypeColors();
        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();

        if (isNetworkAvailable()) {
            loadPokemonList();
        } else {
            showNoInternetError();
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
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingProgress = findViewById(R.id.loading_progress);
        pokemonCount = findViewById(R.id.pokemon_count);
        filterBadge = findViewById(R.id.filter_badge);

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnScanner = findViewById(R.id.btn_scanner);
        ImageButton btnFilter = findViewById(R.id.btn_filter);
        EditText searchBar = findViewById(R.id.search_bar);

        btnBack.setOnClickListener(v -> finish());

        btnScanner.setOnClickListener(v -> {
            Intent intent = new Intent(PokedexActivity.this, ScannerActivity.class);
            startActivity(intent);
        });

        btnFilter.setOnClickListener(v -> showFilterDialog());

        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPokemon(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
    }

    private void setupRecyclerView() {
        // Usar LinearLayoutManager para una columna y cuadros más grandes
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PokemonAdapterImproved(this, displayedList, typeColors);

        adapter.setOnItemClickListener(pokemon -> {
            Intent intent = new Intent(PokedexActivity.this, PokemonDetailActivity.class);
            intent.putExtra("pokemonName", pokemon.getName());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
                Color.parseColor("#D32F2F"),
                Color.parseColor("#EF5350"),
                Color.parseColor("#FF5252")
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isNetworkAvailable()) {
                refreshData();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                showNoInternetError();
            }
        });
    }

    private void refreshData() {
        pokemonList.clear();
        displayedList.clear();
        loadedCount = 0;
        adapter.notifyDataSetChanged();
        loadingProgress.setVisibility(View.VISIBLE);
        loadPokemonList();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetError() {
        Toast.makeText(this, "🔇 Sin conexión a internet", Toast.LENGTH_LONG).show();
        loadingProgress.setVisibility(View.GONE);
    }

    private void showFilterDialog() {
        boolean[] checked = new boolean[types.length];
        for (int i = 0; i < types.length; i++) {
            checked[i] = selectedTypes.contains(types[i]);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🎯 Filtrar por tipo")
                .setMultiChoiceItems(types, checked, (dialogInterface, which, isChecked) -> {
                    if (isChecked) selectedTypes.add(types[which]);
                    else selectedTypes.remove(types[which]);
                })
                .setPositiveButton("Aplicar", (dialogInterface, which) -> {
                    applyFilters();
                    updateFilterBadge();
                })
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Limpiar", (dialogInterface, which) -> {
                    selectedTypes.clear();
                    applyFilters();
                    updateFilterBadge();
                })
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.pokemon_red));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);
    }

    private void updateFilterBadge() {
        if (selectedTypes.isEmpty()) {
            filterBadge.setVisibility(View.GONE);
        } else {
            filterBadge.setVisibility(View.VISIBLE);
            filterBadge.setText(String.valueOf(selectedTypes.size()));
        }
    }

    private void applyFilters() {
        displayedList.clear();
        for (PokemonFull p : pokemonList) {
            if (selectedTypes.isEmpty() || p.getTypes().stream().anyMatch(selectedTypes::contains)) {
                displayedList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
        updatePokemonCount();

        if (!selectedTypes.isEmpty()) {
            Toast.makeText(this, "Filtrando por " + selectedTypes.size() + " tipo(s)", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPokemonList() {
        if (isLoading) return;

        isLoading = true;
        loadingProgress.setVisibility(View.VISIBLE);

        PokeApiService.getApi().getPokemonList().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                isLoading = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    JsonArray results = response.body().getAsJsonArray("results");
                    if (results != null && results.size() > 0) {
                        int limit = Math.min(results.size(), TOTAL_POKEMON);
                        for (int i = 0; i < limit; i++) {
                            String name = results.get(i).getAsJsonObject().get("name").getAsString();
                            loadPokemonDetail(name);
                        }
                    } else {
                        Toast.makeText(PokedexActivity.this, "❌ No se encontraron Pokémon", Toast.LENGTH_SHORT).show();
                        loadingProgress.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(PokedexActivity.this, "❌ Error del servidor", Toast.LENGTH_SHORT).show();
                    loadingProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                isLoading = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(PokedexActivity.this, "📡 Error de conexión", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPokemonDetail(String name) {
        PokeApiService.getApi().getPokemon(name).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject p = response.body();
                        PokemonFull pokemon = new PokemonFull();

                        // Información básica
                        pokemon.setName(capitalizeFirst(p.get("name").getAsString()));
                        pokemon.setNumber(p.get("id").getAsInt());

                        // Tipos
                        List<String> typesList = new ArrayList<>();
                        JsonArray typeArray = p.getAsJsonArray("types");
                        for (int j = 0; j < typeArray.size(); j++) {
                            typesList.add(typeArray.get(j).getAsJsonObject()
                                    .get("type").getAsJsonObject()
                                    .get("name").getAsString());
                        }
                        pokemon.setTypes(typesList);

                        // Altura y peso
                        pokemon.setHeight(p.get("height").getAsDouble());
                        pokemon.setWeight(p.get("weight").getAsDouble());

                        // Habilidades
                        List<String> abilitiesList = new ArrayList<>();
                        JsonArray abilitiesArray = p.getAsJsonArray("abilities");
                        for (int k = 0; k < abilitiesArray.size(); k++) {
                            String abilityName = abilitiesArray.get(k).getAsJsonObject()
                                    .getAsJsonObject("ability").get("name").getAsString();
                            abilitiesList.add(capitalizeFirst(abilityName));
                        }
                        pokemon.setAbilities(abilitiesList);

                        // Estadísticas
                        JsonArray statsArray = p.getAsJsonArray("stats");
                        for (int s = 0; s < statsArray.size(); s++) {
                            JsonObject stat = statsArray.get(s).getAsJsonObject();
                            String statName = stat.getAsJsonObject("stat").get("name").getAsString();
                            int statValue = stat.get("base_stat").getAsInt();

                            switch (statName) {
                                case "hp":
                                    pokemon.setHp(statValue);
                                    break;
                                case "attack":
                                    pokemon.setAttack(statValue);
                                    break;
                                case "defense":
                                    pokemon.setDefense(statValue);
                                    break;
                                case "speed":
                                    pokemon.setSpeed(statValue);
                                    break;
                                case "special-attack":
                                    pokemon.setSpecialAttack(statValue);
                                    break;
                                case "special-defense":
                                    pokemon.setSpecialDefense(statValue);
                                    break;
                            }
                        }

                        // Imagen
                        String img = "";
                        JsonObject sprites = p.getAsJsonObject("sprites");
                        if (sprites != null) {
                            JsonObject other = sprites.getAsJsonObject("other");
                            if (other != null) {
                                JsonObject officialArtwork = other.getAsJsonObject("official-artwork");
                                if (officialArtwork != null && officialArtwork.has("front_default")) {
                                    img = officialArtwork.get("front_default").getAsString();
                                }
                            }
                            if (img.isEmpty() && sprites.has("front_default")) {
                                img = sprites.get("front_default").getAsString();
                            }
                        }
                        pokemon.setImageUrl(img);

                        int left = 0, right = pokemonList.size();
                        while (left < right) {
                            int mid = (left + right) / 2;
                            if (pokemonList.get(mid).getNumber() < pokemon.getNumber()) {
                                left = mid + 1;
                            } else {
                                right = mid;
                            }
                        }
                        pokemonList.add(left, pokemon);

                        loadedCount++;
                        updateDisplayList();
                        updatePokemonCount();

                        if (loadedCount >= TOTAL_POKEMON) {
                            loadingProgress.setVisibility(View.GONE);
                            Toast.makeText(PokedexActivity.this,
                                    "✅ ¡Pokédex cargada!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    private void updateDisplayList() {
        displayedList.clear();
        for (PokemonFull p : pokemonList) {
            boolean matchesType = selectedTypes.isEmpty() || p.getTypes().stream().anyMatch(selectedTypes::contains);
            if (matchesType) {
                displayedList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updatePokemonCount() {
        if (pokemonCount != null) {
            pokemonCount.setText(displayedList.size() + " / " + TOTAL_POKEMON + " Pokémon");
        }
    }

    private void filterPokemon(String query) {
        displayedList.clear();
        for (PokemonFull p : pokemonList) {
            boolean matchesName = p.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesType = selectedTypes.isEmpty() || p.getTypes().stream().anyMatch(selectedTypes::contains);
            if (matchesName && matchesType) {
                displayedList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
        updatePokemonCount();
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}