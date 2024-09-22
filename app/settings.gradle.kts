// settings.gradle.kts

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/")}
        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") } // 카카오맵 저장소 추가 (kakaomap)

    }
}

rootProject.name = "sos"
include(":app")
