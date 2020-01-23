package com.twilio.sample

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class FirebasePushService: FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    Log.d("newToken", token)
  }
}