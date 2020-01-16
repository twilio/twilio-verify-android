package com.twilio.sample

import android.app.Application
import com.google.firebase.FirebaseApp

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class AppApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    FirebaseApp.initializeApp(this)
  }
}