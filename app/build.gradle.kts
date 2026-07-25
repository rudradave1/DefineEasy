import java.util.Properties

// ---------------------------------------------------------------------------
// Release versioning
// ---------------------------------------------------------------------------
// Use explicit release values so the Play upload always reflects the current
// build, even when the working tree changes without a new commit.
val APP_VERSION_CODE = 34
val APP_VERSION_NAME = "4.0.0"

// ---------------------------------------------------------------------------
// Load local.properties for signing secrets
// ---------------------------------------------------------------------------
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) load(localPropsFile.inputStream())
}

val releaseKeystorePath = System.getenv("KEYSTORE_FILE")
    ?: localProperties.getProperty("keystore.file")
    ?: "../upload-keystore.jks"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.rudra.defineeasy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rudra.defineeasy"
        minSdk = 26
        targetSdk = 36
        versionCode = APP_VERSION_CODE
        versionName = APP_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(releaseKeystorePath)
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties.getProperty("keystore.password", "")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("key.password", "")
        }
    }

    val hasSigningCredentials = System.getenv("KEYSTORE_PASSWORD") != null ||
        localProperties.getProperty("keystore.password", "").isNotBlank()

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "CRASHLYTICS_ENABLED", "false")
        }
        release {
            if (hasSigningCredentials) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            buildConfigField("boolean", "CRASHLYTICS_ENABLED", "true")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    lint {
        disable += "StateFlowValueCalledInComposition"
        baseline = file("lint-baseline.xml")
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
            keepDebugSymbols += "**/libdatastore_shared_counter.so"
            keepDebugSymbols += "**/libsqlcipher.so"
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val enableCrashlyticsUpload = providers
    .gradleProperty("enableCrashlyticsUpload")
    .orNull
    ?.toBooleanStrictOrNull()
    ?: false

tasks.matching {
    it.name == "uploadCrashlyticsMappingFileRelease"
}.configureEach {
    enabled = enableCrashlyticsUpload
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.8.4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Compose dependencies
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Coroutines
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Coroutine Lifecycle Scopes
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-android-compiler:2.57.2")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    kaptTest("com.google.dagger:hilt-android-compiler:2.57.2")
    kaptTest("androidx.hilt:hilt-compiler:1.2.0")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.57.2")
    kaptAndroidTest("androidx.hilt:hilt-compiler:1.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("androidx.sqlite:sqlite-ktx:2.6.2")

    // Play Core - In-App Review
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
    arguments {
        arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
    }
}
