package com.example.interfazprueba;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokeApiService {
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";
    private static PokeApiInterface api;

    public static PokeApiInterface getApi() {
        if (api == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(PokeApiInterface.class);
        }
        return api;
    }
}


