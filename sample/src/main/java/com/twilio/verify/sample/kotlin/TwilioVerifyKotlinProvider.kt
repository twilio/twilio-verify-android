package com.twilio.verify.sample.kotlin

import android.content.Context
import com.twilio.verify.sample.networking.AuthenticationProvider

object TwilioVerifyKotlinProvider {
  private lateinit var twilioVerifyAdapter: TwilioVerifyKotlinAdapter

  fun getInstance(
    applicationContext: Context,
    url: String
  ): TwilioVerifyKotlinAdapter {
    if (!this::twilioVerifyAdapter.isInitialized) {
      twilioVerifyAdapter =
        TwilioVerifyKotlinAdapter(applicationContext, authentication = AuthenticationProvider(url))
    }
    return twilioVerifyAdapter
  }
}