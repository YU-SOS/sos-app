plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
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

    implementation(libs.v2.all) // 카카오 전체 모듈 설치
    implementation(libs.v2.user) // 카카오 로그인 API 모듈
    implementation (libs.v2.cert) // 카카오 인증 서비스 API 모듈
    implementation(libs.retrofit) //retrofit 라이브러리
    implementation(libs.converter.gson) //gson 라이브러리(json을 자바 클래스로 변환해줌)
    implementation(libs.okhttp) //okhttp 라이브러리
    implementation(libs.logging.interceptor) //okhttp 라이브러리


    // KakaoMap API
    implementation("com.kakao.maps.open:android:2.11.9")

    // Retrofit for HTTP requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Retrofit converter for JSON (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp (Retrofit's dependency for HTTP)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // JWT 유효성 검사
    implementation("com.auth0.android:jwtdecode:2.0.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
