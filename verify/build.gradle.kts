apply("publish.gradle.kts")
plugins {
  id("com.android.library")
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.kotlinAndroidExtensions)
}
android {
  compileSdkVersion(28)

  defaultConfig {
    minSdkVersion(23)
    targetSdkVersion(28)
    versionCode = Versioning.versionCode
    versionName = Versioning.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
    buildConfigField("String", "BASE_URL", "\"https://verify.twilio.com/v2/\"")
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
      )
    }
  }
  testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
  val securityVersion: String by rootProject
  implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))
  debugImplementation(project(Modules.security))
  releaseImplementation("com.twilio:twilio-security-android:$securityVersion")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
  androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.3.1")
  androidTestImplementation("com.squareup.okhttp3:okhttp-tls:4.3.1")
  androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("junit:junit:4.12")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.robolectric:robolectric:4.3.1")
  testImplementation("androidx.test:core:1.2.0")
  testImplementation("org.mockito:mockito-inline:2.28.2")
}
