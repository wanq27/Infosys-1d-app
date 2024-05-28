plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}


android {
    signingConfigs {
        create("Group46") {
            storeFile = file("keystore.jks")
            storePassword = "infosys1d"
            keyAlias = "Key0"
            keyPassword = "infosys1d"
        }
    }
    namespace = "com.group46.infosys1d"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.group46.infosys1d"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("Group46")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("Group46")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("Group46")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    // Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-dynamic-links")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.google.mlkit:image-labeling:17.0.8")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation ("androidx.recyclerview:recyclerview:1.0.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.squareup.picasso:picasso:2.8")
    testImplementation("junit:junit:4.13.2")
    implementation ("com.google.android.material:material:1.8.0")
}