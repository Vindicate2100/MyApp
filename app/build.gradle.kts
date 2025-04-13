plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    //kotlin("kapt") // Аннотационная обработка Kotlin
    id("com.google.devtools.ksp") version "2.1.20-2.0.0" // Закомментировано для быстрого переключения
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 34
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
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ========== Core Dependencies ==========
    implementation(libs.androidx.core.ktx) // Kotlin extensions
    implementation(libs.androidx.appcompat) // AppCompat
    implementation(libs.material)

    // ========== Jetpack Compose ==========
    implementation(platform(libs.androidx.compose.bom)) // BOM для версий Compose
    implementation(libs.ui) // UI компоненты
    implementation(libs.material3) // Material Design 3
    implementation(libs.androidx.material.icons.extended) // Иконки
    implementation(libs.androidx.activity.compose) // Activity integration
    implementation(libs.androidx.ui.tooling.preview) // Preview поддержка
    debugImplementation(libs.androidx.ui.tooling) // Инструменты разработчика
    debugImplementation(libs.androidx.ui.test.manifest) // Манифест для тестов

    // ========== Navigation ==========
    implementation(libs.androidx.navigation.compose) // Навигация в Compose
    implementation(libs.androidx.hilt.navigation.compose) // Hilt навигация

    // ========== Lifecycle & ViewModel ==========
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose) // ViewModel для Compose
    implementation(libs.androidx.lifecycle.runtime.compose) // Lifecycle для Compose

    // ========== Dependency Injection ==========
    implementation(libs.hilt.android) // Hilt core
    //kapt(libs.hilt.android.compiler) // Обработка аннотаций Hilt
    ksp(libs.hilt.android.compiler) // Альтернатива kapt (закомментировано)

    // ========== Database ==========
    implementation(libs.androidx.room.runtime) // Room runtime
    implementation(libs.room.ktx) // Room Coroutines support
    //kapt(libs.androidx.room.compiler) // Room annotation processor
    ksp(libs.androidx.room.compiler) // Альтернатива kapt (закомментировано)

    // ========== Coroutines ==========
    implementation(libs.kotlinx.coroutines.android) // Coroutines

    // ========== Testing ==========
    testImplementation(libs.junit) // Unit tests
    androidTestImplementation(libs.androidx.junit) // Android JUnit
    androidTestImplementation(libs.androidx.espresso.core) // Espresso
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose BOM для тестов
    androidTestImplementation(libs.androidx.ui.test.junit4) // Compose UI тесты

    implementation (libs.androidx.animation)
    implementation (libs.androidx.foundation)
}

//kapt {
   // correctErrorTypes = true // Исправление типов для Hilt
//}