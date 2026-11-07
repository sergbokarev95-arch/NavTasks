val betaKeystorePath = System.getenv("NAVTASKS_BETA_KEYSTORE_PATH")
val betaSigningPassword = System.getenv("NAVTASKS_BETA_SIGN_PASSWORD")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.navtasks.glasslab"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.navtasks.glasslab"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
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
        }
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
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
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("io.github.kyant0:backdrop:2.0.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
