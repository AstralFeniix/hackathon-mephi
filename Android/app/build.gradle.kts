plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.mephi.tikho"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.mephi.tikho"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.5.0"

        ndk {
            // Rust-ядро и sherpa-onnx собраны для этих двух ABI.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        // Модели должны открываться через AssetManager.openFd() без распаковки.
        noCompress += listOf("bin", "onnx")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    // UniFFI generated bindings use JNA; the @aar artifact contains libjnidispatch.so.
    implementation(mapOf("name" to "jna-5.17.0", "ext" to "aar"))
    // Offline speaker diarization: pyannote segmentation + speaker embeddings.
    implementation(mapOf("name" to "sherpa-onnx-2.0.1", "ext" to "aar"))

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
