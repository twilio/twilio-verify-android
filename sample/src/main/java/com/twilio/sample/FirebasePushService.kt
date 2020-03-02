package com.twilio.sample

import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twilio.verify.models.VerifyPushFactorInput

/*
 * Copyright (c) 2020, Twilio Inc.
 */

private const val typeKey = "type"
private const val verifyFactorType = "verify_factor_push"
private const val factorSidKey = "factor_sid"
private const val verificationCodeKey = "verification_code"

class FirebasePushService : FirebaseMessagingService() {

  private lateinit var twilioVerifyAdapter: TwilioVerifyAdapter

  override fun onNewToken(token: String) {
    Log.d("newToken", token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val bundle = getBundleFromMessage(remoteMessage)
    when (bundle.getString(typeKey)) {
      verifyFactorType -> {
        val factorSid = bundle?.getString(factorSidKey)
        val verificationCode = bundle?.getString(verificationCodeKey)
        if (!this::twilioVerifyAdapter.isInitialized) {
          twilioVerifyAdapter = TwilioVerifyAdapter(applicationContext)
        }
        twilioVerifyAdapter.verifyFactor(
            VerifyPushFactorInput(factorSid, verificationCode), { factor ->
          EventBus.send(VerifiedFactor(factor.sid))
        }, { exception ->
          exception.printStackTrace()
        })
      }
    }
  }

  private fun getBundleFromMessage(remoteMessage: RemoteMessage?): Bundle {
    return Bundle().apply {
      remoteMessage?.data?.entries?.let { entries ->
        for (entry in entries) {
          putString(entry.key, entry.value)
        }
      }
    }
  }
}