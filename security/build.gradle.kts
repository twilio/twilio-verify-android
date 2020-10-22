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
  id(MavenPublish.plugin)
  id(Config.Plugins.androidLibrary)
  id(Config.Plugins.kotlinAndroid)
  id(Config.Plugins.kotlinAndroidExtensions)
  jacoco
}
//endregion

val securityVersionName: String by extra
val securityVersionCode: String by extra

//region Android
android {
  compileSdkVersion(Config.Versions.compileSDKVersion)

  defaultConfig {
    minSdkVersion(Config.Versions.minSDKVersion)
    targetSdkVersion(Config.Versions.targetSDKVersion)
    versionCode = securityVersionCode.toInt()
    versionName = securityVersionName

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

  lintOptions {
    lintConfig = rootProject.file(".lint/config.xml")
    xmlReport = true
    isCheckAllWarnings = true
  }
}
//endregion

//region Publish
val pomPackaging: String by project
val pomGroup: String by project
val pomArtifactId: String by project
/*
 * Maven upload configuration that can be used for any maven repo
 */
tasks {
  "uploadArchives"(Upload::class) {
    repositories {
      withConvention(MavenRepositoryHandlerConvention::class) {
        mavenDeployer {
          withGroovyBuilder {
            MavenPublish.Bintray.repository(
              MavenPublish.Bintray.url to uri(MavenPublish.mavenRepo(project))
            ) {
              MavenPublish.Bintray.authentication(
                MavenPublish.Bintray.userName to MavenPublish.mavenUsername(project),
                MavenPublish.Bintray.password to MavenPublish.mavenPassword(project)
              )
            }
          }
          pom.project {
            withGroovyBuilder {
              MavenPublish.Bintray.version(securityVersionName)
              MavenPublish.Bintray.groupId(pomGroup)
              MavenPublish.Bintray.artifactId(pomArtifactId)
              MavenPublish.Bintray.packaging(pomPackaging)
            }
          }
        }
      }
    }
  }
}

task("bintrayLibraryReleaseCandidateUpload", GradleBuild::class) {
  description = "Publish Security SDK release candidate to internal bintray"
  group = MavenPublish.Bintray.group
  buildName = "Security"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties +
      MavenPublish.Bintray.credentials(
        project,
        "https://api.bintray.com/maven/twilio/internal-releases/twilio-security-android/;publish=1",
        MavenPublish.Bintray.user, MavenPublish.Bintray.apiKey
      )
  )
}

task("bintrayLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Security SDK release to bintray"
  group = MavenPublish.Bintray.group
  buildName = "Security"
  buildFile = file("build.gradle.kts")
  tasks = listOf("assembleRelease", "uploadArchives")
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties + MavenPublish.Bintray.credentials(
      project,
      "https://api.bintray.com/maven/twilio/releases/twilio-security-android/;publish=1",
      MavenPublish.Bintray.user, MavenPublish.Bintray.apiKey
    )
  )
}
//endregion

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
  testImplementation("junit:junit:4.12")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.robolectric:robolectric:4.3.1")
  testImplementation("org.mockito:mockito-inline:2.28.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
