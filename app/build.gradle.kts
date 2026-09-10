import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.baselineprofile)
}

// App version comes from gradle/version.properties (bump via scripts/bump-version.sh)
val versionProps = Properties().apply {
    rootProject.file("gradle/version.properties").inputStream().use { load(it) }
}

android {
    namespace = "com.zenzeros.kimon"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.zenzeros.kimon"
        minSdk = 29
        //noinspection EditedTargetSdkVersion
        targetSdk = 37
        versionCode = versionProps.getProperty("VERSION_CODE", "1").trim().toInt()
        versionName = versionProps.getProperty("VERSION_NAME", "0.0.0").trim()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "kimonreleasepassword"
            keyAlias = "kimon"
            keyPassword = "kimonreleasepassword"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    lint {
        // The app uses ComponentActivity + Compose with no Fragments; this check
        // spuriously flags registerForActivityResult for lacking an androidx.fragment dep.
        disable += "InvalidFragmentVersionForActivityResult"

        // New checks from the AGP 9.4 lint that flag long-standing idioms
        // (getString / Locale.getDefault read directly in composables). Worth
        // cleaning up eventually, but not release-blocking.
        warning += setOf(
            "LocalContextGetResourceValueCall",
            "NonObservableLocale",
        )
    }

    buildToolsVersion = "36.1.0"
}

// Export Room schemas so real migrations can be written from version 5 onward
// instead of destructively wiping the database on every schema change.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Navigation 3 & Serialization
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)

    // Material Kolor
    implementation(libs.material.kolor)

    // Coil Image Loading
    implementation(libs.coil.compose)

    // Health Connect
    implementation(libs.androidx.health.connect)

    // Google Play Services Location & Sleep API
    implementation(libs.play.services.location)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

