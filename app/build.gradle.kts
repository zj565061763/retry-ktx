plugins {
   id("com.android.application")
   id("org.jetbrains.kotlin.android")
}

android {
   namespace = "com.sd.demo.retry_ktx"
   compileSdk = libs.versions.androidCompileSdk.get().toInt()
   defaultConfig {
      targetSdk = libs.versions.androidCompileSdk.get().toInt()
      minSdk = 23
      applicationId = "com.sd.demo.retry_ktx"
      versionCode = 1
      versionName = "1.0"

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      vectorDrawables {
         useSupportLibrary = true
      }
   }

   buildTypes {
      release {
         isMinifyEnabled = false
         proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      }
   }

   buildFeatures {
      compose = true
   }

   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
   }
}

kotlin {
   jvmToolchain(8)
}

dependencies {
   implementation(libs.androidx.compose.foundation)
   implementation(libs.androidx.compose.ui.tooling.preview)
   debugImplementation(libs.androidx.compose.ui.tooling)
   debugImplementation(libs.androidx.compose.ui.test.manifest)
   androidTestImplementation(libs.androidx.compose.ui.test.junit4)

   implementation(libs.androidx.compose.material3)
   implementation(libs.androidx.compose.material.ripple)
   implementation(libs.androidx.compose.material.icons.core)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.lifecycle.viewmodel.compose)
   implementation(libs.androidx.lifecycle.runtime.compose)

   testImplementation(libs.junit)
   androidTestImplementation(libs.androidx.test.ext.junit)
   androidTestImplementation(libs.androidx.test.espresso.core)

   implementation(project(":lib"))

   implementation(libs.sd.network)
}