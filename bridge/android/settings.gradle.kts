pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Local sherpa-onnx AAR, downloaded by scripts/fetch-diarization.ps1
        flatDir { dirs("app/libs") }
    }
}

rootProject.name = "Bridge"
include(":app")
