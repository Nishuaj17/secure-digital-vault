plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.securedigitalvault"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.securedigitalvault"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    // Room (local database)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Lifecycle (ViewModel + LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Biometric authentication
    implementation("androidx.biometric:biometric:1.1.0")

    // Security helpers
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // RecyclerView + CardView (dashboard cards & lists)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Activity/Fragment (photo picker & document picker APIs)
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.fragment:fragment:1.6.2")

    // WorkManager (used later for clipboard auto-clear scheduling)
    implementation("androidx.work:work-runtime:2.9.0")
}