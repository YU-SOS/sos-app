// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

// 여기부터
repositories {
    maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }
}
// 여기까지 카카오맵
