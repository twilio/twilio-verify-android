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

// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(from = "publish.gradle.kts")
plugins {
  id(Config.Plugins.dokka) version Config.Versions.dokka
  id(Config.Plugins.nexus) version (Config.Versions.nexus)

}

buildscript {
  repositories {
    jcenter()
    google()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
  }
  dependencies {
    classpath(Config.Dependencies.androidTools)
    classpath(kotlin(Config.Dependencies.kotlin, Config.Versions.kotlin))
    classpath(Config.Dependencies.googleServices)
    classpath(Config.Dependencies.firebasePerformance)
    classpath(Config.Dependencies.jacoco)
    classpath(Config.Dependencies.apkscale)
    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    maven {
      url = uri("https://twilio.bintray.com/internal-releases")
      credentials {
        username = Config.projectProperty(project, "BINTRAY_USER")
        password = Config.projectProperty(project, "BINTRAY_APIKEY")
      }
    }
  }
  plugins.apply(Config.Plugins.ktlint)
  plugins.apply(Config.Plugins.gitHooks)
}

nexusPublishing {
  repositories {
    sonatype {
      username.set(Config.projectProperty(project, MavenPublish.ossrhUsernameEnv))
      password.set(Config.projectProperty(project, MavenPublish.ossrhPasswordEnv))
      stagingProfileId.set(Config.projectProperty(project,MavenPublish.sonatypeStagingProfileIdEnv))
    }
  }

  clientTimeout.set(java.time.Duration.ofSeconds(300))
  connectTimeout.set(java.time.Duration.ofSeconds(60))
}

task("sonatypeTwilioVerifyReleaseUpload", GradleBuild::class) {
  description = "Publish Twilio Verify SDK release"
  group = "Publishing"
  buildName = "TwilioVerify"
  buildFile = file("build.gradle.kts")
  tasks = listOf(":verify:assembleRelease", ":verify:publishTwilioVerifyPublicationToSonatypeRepository", "closeSonatypeStagingRepository")
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties + MavenPublish.credentials(
      project,
      MavenPublish.signingKeyIdEnv,
      MavenPublish.signingPasswordEnv,
      MavenPublish.signingSecretKeyRingFileEnv,
      MavenPublish.ossrhUsernameEnv,
      MavenPublish.ossrhPasswordEnv,
      MavenPublish.sonatypeStagingProfileIdEnv
    )
  )
}

task("sonatypeTwilioSecurityReleaseUpload", GradleBuild::class) {
  description = "Publish Twilio Security SDK release"
  group = "Publishing"
  buildName = "TwilioSecurity"
  buildFile = file("build.gradle.kts")
  tasks = listOf(":security:assembleRelease", ":security:publishTwilioSecurityPublicationToSonatypeRepository", "closeSonatypeStagingRepository")
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties + MavenPublish.credentials(
      project,
      MavenPublish.signingKeyIdEnv,
      MavenPublish.signingPasswordEnv,
      MavenPublish.signingSecretKeyRingFileEnv,
      MavenPublish.ossrhUsernameEnv,
      MavenPublish.ossrhPasswordEnv,
      MavenPublish.sonatypeStagingProfileIdEnv
    )
  )
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
