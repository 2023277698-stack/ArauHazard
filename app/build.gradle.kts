plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.arauhazard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.arauhazard"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // SWIPEREFRESH FIX: Add this line here
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.volley)

    // Glide and other libraries...
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}