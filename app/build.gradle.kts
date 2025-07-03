plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.shuigongrizhi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shuigongrizhi"
        minSdk = 29
        targetSdk = 35
        
        // 增加资源排除配置
        androidResources {
            localeFilters.addAll(listOf("zh-rCN"))
        }
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 启用矢量图支持
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // 数据库schema导出
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // 启用构建配置字段
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://api.openweathermap.org/data/2.5/\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://api.openweathermap.org/data/2.5/\"")
            
            // 签名配置（生产环境需要配置）
            // signingConfig = signingConfigs.getByName("release")
        }
    }
    
    // 禁用资源压缩以避免R.jar锁定问题
    androidResources {
        noCompress.addAll(listOf("txt", "xml"))
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
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Room database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Date picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // PDF generation
    implementation("com.itextpdf:itext7-core:7.2.5")
    

    
    // Weather API (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // ExoPlayer (media3)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")
    
    implementation("com.google.android.material:material:1.11.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}