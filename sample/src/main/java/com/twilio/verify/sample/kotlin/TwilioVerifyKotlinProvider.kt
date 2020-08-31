package com.twilio.verify.sample.kotlin

import android.content.Context
import com.twilio.verify.TwilioVerify

object TwilioVerifyKotlinProvider {
  private lateinit var twilioVerifyAdapter: TwilioVerifyKotlinAdapter

  fun getInstance(
    applicationContext: Context
  ): TwilioVerifyKotlinAdapter {
    if (!this::twilioVerifyAdapter.isInitialized) {
      twilioVerifyAdapter = TwilioVerifyKotlinAdapter(
        TwilioVerify.Builder(applicationContext)
          .build()
      )
    }
    return twilioVerifyAdapter
  }
}
