plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // 必須加入這個
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}


import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.gary.communitycatdb"
    compileSdk = 36
    //enableLegacyVariantApi = true   // 臨時啟用舊 variant API，讓 Hilt 相容（未來移除）

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "com.gary.communitycatdb"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // 加入這行，將 MAPS_API_KEY 從檔案傳遞給 AndroidManifest
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY") ?: ""
    }
    buildToolsVersion = "36.1.0"

    buildFeatures { compose = true }

    //Kotlin 2.0.20，這個版本已經內建了 Compose 編譯器
    //composeOptions { kotlinCompilerExtensionVersion = "1.6.11" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21 // 建議統一到 17 或 21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        //jvmTarget = "21" // 這裡必須與上面一致
    }

}

dependencies {

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose BOM 2026.02.00
    implementation(platform("androidx.compose:compose-bom:2026.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-ktx:5.1.1")
    implementation("com.google.maps.android:maps-compose:6.1.2")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Room
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
}