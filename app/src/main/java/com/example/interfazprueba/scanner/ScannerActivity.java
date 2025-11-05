package com.example.interfazprueba.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.interfazprueba.PokemonDetailActivity;
import com.example.interfazprueba.R;
import com.example.interfazprueba.scanner.services.ProfesorOakService;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    private static final int GALLERY_REQUEST_CODE = 1001;

    private PreviewView previewView;
    private ImageView imagePreview;
    private Button btnCapture, btnGallery, btnBack;
    private TextView textResult, textInstructions;
    private ProgressBar progressBar;
    private View loadingOverlay;

    private ImageCapture imageCapture;
    private ProfesorOakService profesorOakService;

    private static final String TAG = "ScannerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        imagePreview = findViewById(R.id.imagePreview);
        textResult = findViewById(R.id.textResult);
        textInstructions = findViewById(R.id.textInstructions);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        btnCapture = findViewById(R.id.btnCapture);
        btnGallery = findViewById(R.id.btnGallery);
        btnBack = findViewById(R.id.btnBack);

        profesorOakService = new ProfesorOakService();

        btnBack.setOnClickListener(v -> finish());
        btnCapture.setOnClickListener(v -> capturePhoto());
        btnGallery.setOnClickListener(v -> openGallery());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void startCamera() {
        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build();

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                    runOnUiThread(() -> btnCapture.setEnabled(true));

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error iniciando cámara: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show();
                }
            }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            Log.e(TAG, "Error iniciando cámara: " + e.getMessage());
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Cámara no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getExternalFilesDir(null),
                "pokemon_capture_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        Toast.makeText(this, "Capturando foto...", Toast.LENGTH_SHORT).show();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        runOnUiThread(() -> {
                            imagePreview.setImageBitmap(bitmap);
                            imagePreview.setVisibility(View.VISIBLE);
                            textResult.setText("Analizando...");
                        });

                        profesorOakService.clasificarPokemon(bitmap, new ProfesorOakService.PokemonCallback() {
                            @Override
                            public void onSuccess(String predictionJson) {
                                procesarPrediccion(predictionJson);
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> textResult.setText("Error: " + error));
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error al capturar imagen: " + exception.getMessage());
                        Toast.makeText(ScannerActivity.this, "Error al capturar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void procesarPrediccion(String predictionJson) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(predictionJson);
                String pokemonName = json.getString("pokemon");
                double confidence = json.getDouble("confidence");

                textResult.setText("Detectado: " + pokemonName + " (" + confidence + "%)");

                Intent intent = new Intent(ScannerActivity.this, PokemonDetailActivity.class);
                intent.putExtra("pokemonName", pokemonName.toLowerCase());
                intent.putExtra("openedFromScanner", true);
                startActivity(intent);

            } catch (JSONException e) {
                textResult.setText("Error procesando resultado");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                imagePreview.setImageBitmap(bitmap);
                imagePreview.setVisibility(View.VISIBLE);
                textResult.setText("Analizando...");

                profesorOakService.clasificarPokemon(bitmap, new ProfesorOakService.PokemonCallback() {
                    @Override
                    public void onSuccess(String predictionJson) {
                        procesarPrediccion(predictionJson);
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> textResult.setText("Error: " + error));
                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
