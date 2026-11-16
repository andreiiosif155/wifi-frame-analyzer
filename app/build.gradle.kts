plugins {
    id("com.android.application") version "9.2.1"
}

    android {
    namespace = "com.example.wifianalyzer"
        compileSdk = 37
    defaultConfig {
      applicationId = "com.example.wifianalyzer"
    minSdk = 26
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
          release {
          }
      }
    }

  dependencies {
      testImplementation("junit:junit:4.13.2")
  }
