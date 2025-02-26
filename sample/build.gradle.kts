/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

apply(from = "../jacoco.gradle.kts")
plugins {
  id(Config.Plugins.androidApplication)
  id(Config.Plugins.googleServices)
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.firebasePerformance)
  jacoco
}
repositories {
  mavenLocal()
}

val verifyVersionName: String by rootProject.allprojects.first { it.name == Modules.verify }.extra
val verifyVersionCode: String by rootProject.allprojects.first { it.name == Modules.verify }.extra

android {
  namespace = "com.twilio.verify.sample"
  compileSdk = Config.Versions.compileSDKVersion
  defaultConfig {
    applicationId = "com.twilio.verify.sample"
    minSdk = Config.Versions.minSDKVersion
    targetSdk = Config.Versions.targetSDKVersion
    layout.buildDirectory
    versionName = verifyVersionName
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
    getByName("debug") {
      signingConfig = signingConfigs.getByName("release")
    }
  }

  lint {
    lintConfig = rootProject.file(".lint/config.xml")
    checkAllWarnings = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    viewBinding = true
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))
  debugImplementation(project(":${Modules.verify}"))
  releaseImplementation("com.twilio:twilio-verify-android:0.8.0")
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Config.Versions.kotlin}")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.0")
  implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
  implementation("com.google.firebase:firebase-messaging-ktx")
  implementation("com.google.firebase:firebase-analytics-ktx")
  implementation("com.google.firebase:firebase-perf-ktx")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.navigation:navigation-fragment-ktx:2.8.7")
  implementation("androidx.navigation:navigation-ui-ktx:2.8.7")
  implementation("io.insert-koin:koin-android:3.5.3")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.robolectric:robolectric:4.14.1")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.mockito:mockito-inline:3.11.2")
  testImplementation("androidx.test:core:1.6.1")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
