plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    kotlin("kapt")

    // Navigation Safe Args
    id("androidx.navigation.safeargs")

    id("kotlin-parcelize")
}

android {
    namespace = "com.example.sharidev2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sharidev2"
        minSdk = 26
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
    implementation("androidx.core:core-i18n:1.0.0-alpha01")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Products
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-auth")                     // Dependency for the Firebase Authentication library
    implementation("com.google.android.gms:play-services-auth:20.7.0")      // Dependency for the Google Play services library and specify its version

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Swipe-to-Refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-places:17.0.0")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.google.android.libraries.places:places:2.4.0")
    implementation("com.android.volley:volley:1.2.0")

    // Viewpager 2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Date Time Picker
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation("com.github.swnishan:materialdatetimepicker:1.0.0")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Gson
    implementation("com.google.code.gson:gson:2.8.8")

}
