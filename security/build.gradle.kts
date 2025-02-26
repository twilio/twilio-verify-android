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

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

tasks.withType<Test>().configureEach {
  jvmArgs(
    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-exports=java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
    "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens=java.base/java.io=ALL-UNNAMED",
    "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
    "--add-opens=java.base/javax.crypto=ALL-UNNAMED"
  )
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
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Config.Versions.kotlin}")
  compileOnly("androidx.biometric:biometric:1.1.0")
  testImplementation("junit:junit:4.13.2")
  testImplementation("io.mockk:mockk:1.13.16")
  testImplementation("org.robolectric:robolectric:4.14.1")
  testImplementation("androidx.test:core:1.6.1")
  testImplementation("org.hamcrest:hamcrest-library:2.2")
  testImplementation("androidx.biometric:biometric:1.1.0")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
