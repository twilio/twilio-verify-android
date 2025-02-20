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

//region Plugins
apply(from = "../jacoco.gradle.kts")
plugins {
  id(Config.Plugins.androidLibrary)
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.maven_publish)
  id(Config.Plugins.signing)
  jacoco
}
//endregion

val securityVersionName: String by extra
val securityVersionCode: String by extra

//region Android
android {
  namespace = "com.twilio.security"
  compileSdk = Config.Versions.compileSDKVersion
  testOptions.unitTests.isIncludeAndroidResources = true
  defaultConfig {
    minSdk = Config.Versions.minSDKVersion
    targetSdk = Config.Versions.targetSDKVersion
    version = securityVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
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

  lint {
    lintConfig = rootProject.file(".lint/config.xml")
    xmlReport = true
    checkAllWarnings = true
  }
}
//endregion

//region Publish
val pomGroup: String by project
val pomArtifactId: String by project

publishing {
  publications {
    create<MavenPublication>("TwilioSecurity") {
      groupId = pomGroup
      artifactId = pomArtifactId
      version = securityVersionName
      artifact("${layout.buildDirectory}/outputs/aar/security-release.aar")

      pom.withXml {
        asNode().apply {
          appendNode("name", "twilio-security-android")
          appendNode("description", "Twilio Security library.")
          appendNode("url", "https://github.com/twilio/twilio-verify-android")
          appendNode("licenses").apply {
            appendNode("license").apply {
              appendNode("name", "Apache License, Version 2.0")
              appendNode("url", "https://github.com/twilio/twilio-verify-android/blob/main/LICENSE")
            }
          }
          appendNode("developers").apply {
            appendNode("developer").apply {
              appendNode("id", "Twilio")
              appendNode("name", "Twilio")
            }
          }
          appendNode("scm").apply {
            appendNode("connection", "scm:git:github.com/twilio/twilio-verify-android.git")
            appendNode("developerConnection", "scm:git:ssh://github.com/twilio/twilio-verify-android.git")
            appendNode("url", "https://github.com/twilio/twilio-verify-android/tree/main")
          }
          appendNode("dependencies").apply {
            project.configurations["releaseImplementation"].allDependencies.forEach {
              appendNode("dependency").apply {
                appendNode("groupId", it.group)
                appendNode("artifactId", it.name)
                appendNode("version", it.version)
              }
            }
          }
        }
      }
    }
  }
}

signing {
  sign(publishing.publications)
}

kotlin {
  jvmToolchain(17)
}
//endregion

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
  compileOnly("androidx.biometric:biometric:1.1.0")
  testImplementation("junit:junit:4.12")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.robolectric:robolectric:4.4")
  testImplementation("androidx.test:core:1.2.0")
  testImplementation("org.hamcrest:hamcrest-library:1.3")
  testImplementation("org.mockito:mockito-inline:2.28.2")
  testImplementation("androidx.biometric:biometric:1.1.0")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
