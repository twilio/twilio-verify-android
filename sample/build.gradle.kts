apply(from = "../jacoco.gradle.kts")
plugins {
  id(Config.Plugins.androidApplication)
  id(Config.Plugins.googleServices)
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.kotlinAndroidExtensions)
  id(Config.Plugins.firebasePerformance)
  id(Config.Plugins.versionBumper)
  jacoco
}
android {
  compileSdkVersion(Config.Versions.compileSDKVersion)
  defaultConfig {
    applicationId = "com.twilio.verify.sample"
    minSdkVersion(Config.Versions.minSDKVersion)
    targetSdkVersion(Config.Versions.targetSDKVersion)
    versionCode = versionBumper.versionCode
    versionName = versionBumper.versionName
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  signingConfigs {
    create("release") {
      storeFile = file("release-key.keystore")
      storePassword = "verify"
      keyAlias = "verify"
      keyPassword = "verify"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
  }

  lintOptions {
    lintConfig = rootProject.file(".lint/config.xml")
    xmlReport = true
    isCheckAllWarnings = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}

dependencies {
  val verifyVersion = "0.0.3"
  implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))
  debugImplementation(project(Modules.verify))
  releaseImplementation("com.twilio:twilio-verify-android:$verifyVersion")
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Config.Versions.kotlin}")
  implementation("androidx.appcompat:appcompat:1.1.0")
  implementation("androidx.core:core-ktx:1.2.0")
  implementation("androidx.constraintlayout:constraintlayout:1.1.3")
  implementation("com.google.firebase:firebase-analytics:17.4.1")
  implementation("com.google.firebase:firebase-messaging:20.1.7")
  implementation("com.google.firebase:firebase-perf:19.0.7")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")
  implementation("com.squareup.okhttp3:okhttp:4.4.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.4.0")
  implementation("com.google.android.material:material:1.1.0")
  implementation("androidx.navigation:navigation-fragment-ktx:2.2.2")
  implementation("androidx.navigation:navigation-ui-ktx:2.2.2")
  implementation("org.koin:koin-android:2.1.5")
  implementation("org.koin:koin-androidx-viewmodel:2.1.5")
  testImplementation("junit:junit:4.12")
  testImplementation("org.robolectric:robolectric:4.3.1")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.mockito:mockito-inline:2.28.2")
  testImplementation("androidx.test:core:1.2.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.3.1")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
