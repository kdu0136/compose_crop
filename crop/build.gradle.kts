@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace = "com.dongun.crop"
    compileSdk = Configurations.compileSdk

    defaultConfig {
        minSdk = Configurations.minSdk
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Configurations.kotlinCompilerExtension
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.preview)
//    implementation(libs.compose.material)
//    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
//    implementation(libs.compose.animation.graphics)

    implementation("com.github.SmartToolFactory:Compose-Extended-Gestures:3.0.0")
}