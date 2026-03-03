plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // 必須加入這個
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
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
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildToolsVersion = "36.1.0"

    buildFeatures { compose = true }

    //Kotlin 2.0.20，這個版本已經內建了 Compose 編譯器
    //composeOptions { kotlinCompilerExtensionVersion = "1.6.11" }
}

dependencies {

    implementation("com.google.android.material:material:1.9.0")

    // Compose BOM 2026.02.00
    implementation(platform("androidx.compose:compose-bom:2026.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Google Maps Compose 最新
    implementation("com.google.maps.android:maps-compose:8.2.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

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