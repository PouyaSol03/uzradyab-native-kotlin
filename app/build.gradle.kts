plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.uzradyab"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.exir.uzradyab"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "EXIR_TILE_BASE_URL",
            "\"https://map.exirfirm.com/tile/\""
        )
        buildConfigField(
            "String",
            "MAP_IR_API_KEY",
            "\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImNjYTg5MGViMzJlNzA4N2Q0ZDI3MjI5ZDBjMmZkYjFkOTRlNWQyOTUyNDc3NzhjN2M1Y2YxYmFkNzhiMjFkMGQ5NDNkMzg2ZTc3MDBhNGE1In0.eyJhdWQiOiI0MDAzMyIsImp0aSI6ImNjYTg5MGViMzJlNzA4N2Q0ZDI3MjI5ZDBjMmZkYjFkOTRlNWQyOTUyNDc3NzhjN2M1Y2YxYmFkNzhiMjFkMGQ5NDNkMzg2ZTc3MDBhNGE1IiwiaWF0IjoxNzc3Nzk3Nzg2LCJuYmYiOjE3Nzc3OTc3ODYsImV4cCI6MTc4MDM4OTc4Niwic3ViIjoiIiwic2NvcGVzIjpbImJhc2ljIl19.WsvBtor5Xp1MPC1hF2I8kea6iAzOCyc_skxeTmSNDzUeLdlMe5nhqCMdG7lGbIKEQTGKnZMUVPBoiZ0rsLtDBwmTMAUVtrvkucqBBccQBXIFH5vZslpVVbwyDHjSm9farffrORQX7Rn-MnhSOPAfUqap2gSYPyehtQFSm8Lqb3Zlst1pr6_z_0ki41Ln-wMaWChHA66w38mVYCB0o8kzDBb5zvl1ZQKBvQjLH7CWNeT4l5BlsnYOM8Rn96xX-yjT6bfC77jl0-s5mxtkRoJHiR26hOFM3t_ZhY9cFQpPINc7oWbKe-l0a-rPg2ipBjukqJpdouJVjuVunDP0amCuhg\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.osmdroid.android)
    implementation(libs.androidx.biometric)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.play.services.auth.api.phone)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
