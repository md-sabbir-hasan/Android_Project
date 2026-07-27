plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.nexaerp.mobile"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.nexaerp.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Development backend URL.
        // Android Emulator থেকে নিজের PC-এর localhost access করতে 10.0.2.2 লাগে।
        buildConfigField(
            "String",
            "BASE_URL",
            "\"http://10.0.2.2:8085/\""
        )
    }

    buildTypes {

        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Existing Android dependencies
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Fragment
    implementation("androidx.fragment:fragment:1.8.9")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.9.7")
    implementation("androidx.navigation:navigation-ui:2.9.7")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.11.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.11.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Swipe-to-refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}