plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false

    // Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}
