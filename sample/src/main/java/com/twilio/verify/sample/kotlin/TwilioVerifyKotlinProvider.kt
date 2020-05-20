package com.twilio.verify.sample.kotlin

import android.content.Context

object TwilioVerifyKotlinProvider {
  private lateinit var twilioVerifyAdapter: TwilioVerifyKotlinAdapter

  fun getInstance(
    applicationContext: Context
  ): TwilioVerifyKotlinAdapter {
    if (!this::twilioVerifyAdapter.isInitialized) {
      twilioVerifyAdapter =
        TwilioVerifyKotlinAdapter(applicationContext)
    }
    return twilioVerifyAdapter
  }
}