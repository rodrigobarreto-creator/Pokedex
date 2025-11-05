plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.interfazprueba"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.interfazprueba"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ¡AGREGAR ESTA DEPENDENCIA PARA EL JUEGO!
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Volley (opcional si ya usas Retrofit)
    implementation("com.android.volley:volley:1.2.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Room (si lo necesitas)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // CameraX
    val cameraVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // OkHttp (para conexión con la API del modelo)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Guava (requerido por CameraX para ListenableFuture)
    implementation("com.google.guava:guava:32.1.2-android")

    // Text to Speach
    implementation("androidx.core:core-ktx:1.13.1")

}
