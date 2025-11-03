package com.example.interfazprueba;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PokeApi {

    // Obtener todos los Pokémon con límite
    @GET("pokemon")
    Call<JsonObject> getAllPokemon(@Query("limit") int limit);

    // Obtener un Pokémon por nombre
    @GET("pokemon/{name}")
    Call<JsonObject> getPokemon(@Path("name") String name);

    // Obtener directamente por URL completa
    @GET
    Call<JsonObject> getByUrl(@Url String url);
}

