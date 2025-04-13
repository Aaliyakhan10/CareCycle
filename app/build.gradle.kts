import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.carecycle"
    compileSdk = 35
    val localProperties = Properties().apply {
        load(rootProject.file("local.properties").inputStream())
    }

    buildFeatures{
        viewBinding=true
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.carecycle"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Define keys
    buildConfigField("String", "MAPS_API_KEY", "\"${localProperties["MAPS_API_KEY"]}\"")
    buildConfigField("String", "IMGBB_API_KEY", "\"${localProperties["IMGBB_API_KEY"]}\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }



}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.github.MarsadMaqsood:StylishDialogs:1.0.0")
    implementation ("io.github.shashank02051997:FancyToast:2.0.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.fragment:fragment-ktx:1.6.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")

        implementation ("com.squareup.retrofit2:retrofit:2.9.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("com.firebase:geofire-android-common:3.2.0")
    implementation("com.firebase:geofire-android:3.2.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-database-ktx")

    implementation("com.google.android.gms:play-services-location:21.2.0")






}