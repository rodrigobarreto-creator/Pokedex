package com.example.interfazprueba;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.interfazprueba.scanner.ScannerActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokedexActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private List<PokemonFull> pokemonList = new ArrayList<>();
    private List<PokemonFull> displayedList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private final String[] types = {"normal", "fighting", "flying", "poison", "ground",
            "rock", "bug", "ghost", "steel", "fire", "water", "grass",
            "electric", "psychic", "ice", "dragon", "dark", "fairy"};

    private Set<String> selectedTypes = new HashSet<>();
    private boolean isLoading = false;
    private int loadedCount = 0;
    private final int TOTAL_POKEMON = 151; // Solo primera generación para mejor performance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();

        // Verificar conexión a internet antes de cargar
        if (isNetworkAvailable()) {
            loadPokemonList();
        } else {
            showNoInternetError();
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PokemonAdapter(this, displayedList);

        adapter.setOnItemClickListener(pokemon -> {
            Intent intent = new Intent(PokedexActivity.this, PokemonDetailActivity.class);
            intent.putExtra("pokemonName", pokemon.getName());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
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
        loadPokemonList();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetError() {
        Toast.makeText(this, "Sin conexión a internet. Conéctate e intenta nuevamente.", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(() -> {
            if (pokemonList.isEmpty()) {
                Toast.makeText(this, "Usa el botón de actualizar ↻ para reintentar", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    private void showFilterDialog() {
        boolean[] checked = new boolean[types.length];
        for (int i = 0; i < types.length; i++) {
            checked[i] = selectedTypes.contains(types[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por tipo")
                .setMultiChoiceItems(types, checked, (dialog, which, isChecked) -> {
                    if (isChecked) selectedTypes.add(types[which]);
                    else selectedTypes.remove(types[which]);
                })
                .setPositiveButton("Aplicar", (dialog, which) -> applyFilters())
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Limpiar", (dialog, which) -> {
                    selectedTypes.clear();
                    applyFilters();
                })
                .show();
    }

    private void applyFilters() {
        displayedList.clear();
        for (PokemonFull p : pokemonList) {
            if (selectedTypes.isEmpty() || p.getTypes().stream().anyMatch(selectedTypes::contains)) {
                displayedList.add(p);
            }
        }
        adapter.notifyDataSetChanged();

        if (!selectedTypes.isEmpty()) {
            Toast.makeText(this, "Filtrando por: " + selectedTypes, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPokemonList() {
        if (isLoading) return;

        isLoading = true;
        Toast.makeText(this, "Cargando Pokémon...", Toast.LENGTH_SHORT).show();

        // CORREGIDO: Llamada sin parámetros extra
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
                        // Limitar a los primeros 151 Pokémon
                        int limit = Math.min(results.size(), TOTAL_POKEMON);
                        for (int i = 0; i < limit; i++) {
                            String name = results.get(i).getAsJsonObject().get("name").getAsString();
                            loadPokemonDetail(name);
                        }
                    } else {
                        Toast.makeText(PokedexActivity.this, "No se encontraron Pokémon", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PokedexActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                isLoading = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(PokedexActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
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
                        pokemon.setName(p.get("name").getAsString());
                        pokemon.setNumber(p.get("id").getAsInt());

                        List<String> typesList = new ArrayList<>();
                        JsonArray typeArray = p.getAsJsonArray("types");
                        for (int j = 0; j < typeArray.size(); j++) {
                            typesList.add(typeArray.get(j).getAsJsonObject()
                                    .get("type").getAsJsonObject()
                                    .get("name").getAsString());
                        }
                        pokemon.setTypes(typesList);

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
                            // Fallback a sprite por defecto
                            if (img.isEmpty() && sprites.has("front_default")) {
                                img = sprites.get("front_default").getAsString();
                            }
                        }
                        pokemon.setImageUrl(img);

                        // Insertar ordenadamente
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

                        if (loadedCount % 20 == 0) {
                            Toast.makeText(PokedexActivity.this,
                                    "Cargados " + loadedCount + "/" + TOTAL_POKEMON + " Pokémon",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (loadedCount >= TOTAL_POKEMON) {
                            Toast.makeText(PokedexActivity.this,
                                    "¡Todos los Pokémon cargados!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(PokedexActivity.this, "Error procesando Pokémon: " + name, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PokedexActivity.this, "Error cargando: " + name, Toast.LENGTH_SHORT).show();
                t.printStackTrace();
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
    }
}
