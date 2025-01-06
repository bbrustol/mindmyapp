plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.bbrustol.mmm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bbrustol.mmm"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(project(":feature:mindmylib"))
    implementation(project(":core:ui"))
    implementation(project(":core:infrastructure"))

    implementation(libs.androidx.core.ktx)

    implementation(libs.bundles.compose)

    implementation(libs.bundles.koin)

}