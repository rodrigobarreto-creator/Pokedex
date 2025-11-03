package com.example.interfazprueba;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface PokeApiInterface {

    // ✅ Lista de Pokémon (ej. primeros 151)
    @GET("pokemon?limit=151")
    Call<JsonObject> getPokemonList();

    // ✅ Info general del Pokémon (stats, tipos, sprites, etc.)
    @GET("pokemon/{name}")
    Call<JsonObject> getPokemon(@Path("name") String name);

    // ✅ Info de especie (descripción, evolución, etc.)
    @GET("pokemon-species/{name}")
    Call<JsonObject> getPokemonSpecies(@Path("name") String name);

    // ✅ Cadena evolutiva: recibe la URL completa (porque viene así en species)
    @GET
    Call<JsonObject> getEvolutionChain(@Url String url);
}
