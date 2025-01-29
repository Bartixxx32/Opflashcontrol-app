plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.autonomousapps.dependency-analysis")
}

android {
    namespace = "com.bartixxx.opflashcontrol"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bartixxx.opflashcontrol"
        minSdk = 31
        targetSdk = 35

        versionCode = 31
        versionName = "1.3.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            multiDexEnabled = true
        }
        getByName("debug") {
            multiDexEnabled = true
            isJniDebuggable = true
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
        viewBinding = true
    }
    buildToolsVersion = "35.0.0"
}

tasks.register("getVersion") {
    doLast {
        val versionCode = android.defaultConfig.versionCode
        val versionName = android.defaultConfig.versionName
        val versionFile = File("app/build/version.txt")

        // Output the version details
        println("App Version Code: $versionCode")
        println("App Version Name: $versionName")
        versionFile.parentFile.mkdirs()
        if (!versionFile.exists()) {
            versionFile.createNewFile()
        }
        // Optionally, write the version details to a file
        versionFile.writeText("$versionName")
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugRuntimeOnly(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.unit)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.viewmodel)
}
