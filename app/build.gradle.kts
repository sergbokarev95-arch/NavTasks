val betaKeystorePath = System.getenv("NAVTASKS_BETA_KEYSTORE_PATH")
val betaSigningPassword = System.getenv("NAVTASKS_BETA_SIGN_PASSWORD")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.navtasks.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.navtasks.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 100
        versionName = "0.4.75"
    }

    signingConfigs {
        if (!betaKeystorePath.isNullOrBlank() && !betaSigningPassword.isNullOrBlank()) {
            create("betaStable") {
                storeFile = file(betaKeystorePath)
                storePassword = betaSigningPassword
                keyAlias = "navtasks-beta"
                keyPassword = betaSigningPassword
                storeType = "pkcs12"
            }
        }
    }

    buildTypes {
        debug {
            signingConfigs.findByName("betaStable")?.let { signingConfig = it }
            // Signed in-place Beta update; same installed NavTasks app ID and key.
            // Do not uninstall: preserve stored profiles and task history.
            applicationIdSuffix = ".glass"
            versionNameSuffix = "-beta"
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.04.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    // Portable true backdrop blur for our floating navigation (HONOR, Samsung, etc.).
    implementation("dev.chrisbanes.haze:haze:1.7.1")
    // Direct-scene Backdrop 1.0.6 accepted in Glass Lab 0.1.5.
    implementation("io.github.kyant0:backdrop:1.0.6")
    implementation("io.github.kyant0:shapes:1.2.0")
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc14")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    // Real JSON parser for local JVM profile serialization regression tests.
    testImplementation("org.json:json:20250517")
}
