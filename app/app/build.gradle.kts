plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.sos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sos"
        minSdk = 31
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // KakaoMap API
    implementation("com.kakao.maps.open:android:2.11.9")

    // Retrofit for HTTP requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Retrofit converter for JSON (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp (Retrofit's dependency for HTTP)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // JWT Token handling (java-jwt library)
    implementation("com.auth0.android:jwtdecode:2.0.0")

    // JWT 유효성 검사
    implementation("com.auth0.android:jwtdecode:2.0.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
