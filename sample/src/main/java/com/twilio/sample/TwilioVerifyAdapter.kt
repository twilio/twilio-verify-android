/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample

import android.content.Context
import com.twilio.verify.TwilioVerify
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.UpdateChallengeInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.sample.BuildConfig

class TwilioVerifyAdapter(
  applicationContext: Context,
  private val twilioVerify: TwilioVerify = TwilioVerify.Builder(
      applicationContext, Authorization(BuildConfig.ACCOUNT_SID, BuildConfig.AUTH_TOKEN)
  ).build()
) {
  fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (Exception) -> Unit
  ) {
    twilioVerify.createFactor(factorInput, success, error)
  }

  fun verifyFactor(
    verifyFactorInput: VerifyFactorInput,
    success: (Factor) -> Unit,
    error: (Exception) -> Unit
  ) {
    twilioVerify.verifyFactor(verifyFactorInput, success, error)
  }

  fun updateChallenge(
    updateChallengeInput: UpdateChallengeInput,
    success: () -> Unit,
    error: (Exception) -> Unit
  ) {
    twilioVerify.updateChallenge(updateChallengeInput, success, error)
  }
}