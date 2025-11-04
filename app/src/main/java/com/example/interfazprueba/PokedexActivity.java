package com.example.interfazprueba;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private final String[] types = {"normal", "fighting", "flying", "poison", "ground",
            "rock", "bug", "ghost", "steel", "fire", "water", "grass",
            "electric", "psychic", "ice", "dragon", "dark", "fairy"};

    private Set<String> selectedTypes = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PokemonAdapter(this, displayedList);

        adapter.setOnItemClickListener(pokemon -> {
            Intent intent = new Intent(PokedexActivity.this, PokemonDetailActivity.class);
            intent.putExtra("pokemonName", pokemon.getName());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnScanner = findViewById(R.id.btn_scanner);
        ImageButton btnFilter = findViewById(R.id.btn_filter);
        EditText searchBar = findViewById(R.id.search_bar);

        btnBack.setOnClickListener(v -> finish());

        // 🔹 NUEVO: abrir el escáner
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

        loadPokemonList();
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
        PokeApiService.getApi().getPokemonList().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonArray results = response.body().getAsJsonArray("results");
                    for (int i = 0; i < results.size(); i++) {
                        String name = results.get(i).getAsJsonObject().get("name").getAsString();
                        loadPokemonDetail(name);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PokedexActivity.this, "Error al cargar Pokémon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPokemonDetail(String name) {
        PokeApiService.getApi().getPokemon(name).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
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

                    String img = p.getAsJsonObject("sprites")
                            .getAsJsonObject("other")
                            .getAsJsonObject("official-artwork")
                            .get("front_default").getAsString();
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

                    displayedList.clear();
                    displayedList.addAll(pokemonList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
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
