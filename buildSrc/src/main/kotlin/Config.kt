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

import org.gradle.api.Project

object Config {
  object Dependencies {
    const val kotlin = "gradle-plugin"
    const val androidTools = "com.android.tools.build:gradle:${Versions.gradle}"
    const val googleServices = "com.google.gms:google-services:${Versions.googleServices}"
    const val firebasePerformance =
      "com.google.firebase:perf-plugin:${Versions.firebasePerformance}"
    const val jacoco = "org.jacoco:org.jacoco.core:${Versions.jacoco}"
    const val apkscale = "com.twilio:apkscale:${Versions.apkscale}"
  }

  object Plugins {
    const val androidLibrary = "com.android.library"
    const val androidApplication = "com.android.application"
    const val googleServices = "com.google.gms.google-services"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val firebasePerformance = "com.google.firebase.firebase-perf"
    const val dokka = "org.jetbrains.dokka"
    const val apkscale = "com.twilio.apkscale"
    const val nexus = "io.github.gradle-nexus.publish-plugin"
    const val maven_publish = "maven-publish"
    const val signing = "signing"
    const val ktlint = "plugins.ktlint"
    const val gitHooks = "plugins.git-hooks"
    const val sonarqube = "org.sonarqube"
  }

  object Versions {
    const val kotlin = "1.3.72"
    const val gradle = "4.0.1"
    const val googleServices = "4.3.3"
    const val firebasePerformance = "1.3.1"
    const val compileSDKVersion = 29
    const val minSDKVersion = 23
    const val targetSDKVersion = 29
    const val dokka = "1.4.0-rc"
    const val jacoco = "0.8.5"
    const val apkscale = "0.1.0"
    const val nexus = "1.0.0"
    const val sonarqube = "3.2.0"
  }

  object Groups {
    const val git_hooks = "git_hooks"
  }

  fun projectProperty(
    project: Project,
    property: String
  ): String {
    val value =
      if (project.hasProperty(property)) project.property(property) as? String else System.getenv(
          property
      )
    return value ?: ""
  }
}
