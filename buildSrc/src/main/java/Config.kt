import org.gradle.api.Project

/*
 * Copyright (c) 2020, Twilio Inc.
 */

object Config {
  object Dependencies {
    const val kotlin = "gradle-plugin"
    const val androidTools = "com.android.tools.build:gradle:${Versions.gradle}"
    const val googleServices = "com.google.gms:google-services:${Versions.googleServices}"
    const val firebasePerformance =
      "com.google.firebase:perf-plugin:${Versions.firebasePerformance}"
    const val versionBumper = "com.twilio:versionbumper:${Versions.versionBumper}"
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
    const val versionBumper = "com.twilio.versionbumper"
    const val apkscale = "com.twilio.apkscale"
  }

  object Versions {
    const val kotlin = "1.3.72"
    const val gradle = "4.0.1"
    const val googleServices = "4.3.3"
    const val firebasePerformance = "1.3.1"
    const val versionBumper = "0.0.1"
    const val compileSDKVersion = 29
    const val minSDKVersion = 23
    const val targetSDKVersion = 29
    const val dokka = "1.4.0-rc"
    const val jacoco = "0.8.5"
    const val apkscale = "0.1.0"
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