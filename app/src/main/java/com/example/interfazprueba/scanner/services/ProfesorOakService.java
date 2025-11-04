package com.example.interfazprueba.scanner.services;

import android.graphics.Bitmap;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ProfesorOakService {
    private static final String TAG = "ProfesorOakService";
    private static final String BASE_URL = "http://192.168.100.178:8002";

    private OkHttpClient client;

    public ProfesorOakService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public interface PokemonCallback {
        void onSuccess(String prediction);
        void onError(String error);
    }

    public void clasificarPokemon(Bitmap bitmap, PokemonCallback callback) {
        new Thread(() -> {
            try {
                String baseUrl = BASE_URL;
                Log.d(TAG, "Conectando a: " + baseUrl + "/classify/");

                // Redimensionar imagen a 224x224 (requerimiento del modelo)
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

                // Convertir Bitmap a bytes
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageBytes = stream.toByteArray();

                Log.d(TAG, "Imagen preparada, tamaño: " + imageBytes.length + " bytes");

                // Crear solicitud multipart
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "pokemon.jpg",
                                RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                        .build();

                Request request = new Request.Builder()
                        .url(baseUrl + "/classify/")
                        .post(requestBody)
                        .build();

                Log.d(TAG, "Enviando solicitud...");

                try (Response response = client.newCall(request).execute()) {
                    Log.d(TAG, "Código de respuesta: " + response.code());
                    Log.d(TAG, "Mensaje de respuesta: " + response.message());

                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body().string();
                        Log.i(TAG, "Clasificación exitosa: " + result);
                        callback.onSuccess(result);
                    } else {
                        String error = "Error HTTP: " + response.code() + " - " + response.message();
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error en clasificación: " + e.getMessage(), e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }
}
