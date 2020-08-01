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
    }

    object Plugins {
        const val androidApplication = "com.android.application"
        const val googleServices = "com.google.gms.google-services"
        const val kotlinAndroid = "kotlin-android"
        const val kotlinAndroidExtensions = "kotlin-android-extensions"
        const val firebasePerformance = "com.google.firebase.firebase-perf"
    }
}