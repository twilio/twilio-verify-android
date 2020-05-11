/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample

import android.content.Context
import com.twilio.sample.kotlin.TwilioVerifyKotlinProvider
import com.twilio.sample.model.CreateFactorData
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.Factor
import com.twilio.verify.models.UpdateChallengeInput
import com.twilio.verify.models.VerifyFactorInput

interface TwilioVerifyAdapter {
  fun createFactor(
    createFactorData: CreateFactorData,
    onSuccess: (Factor) -> Unit,
    onError: (Exception) -> Unit
  )

  fun verifyFactor(
    verifyFactorInput: VerifyFactorInput,
    onSuccess: (Factor) -> Unit,
    onError: (TwilioVerifyException) -> Unit
  )

  fun getChallenge(
    challengeSid: String,
    factorSid: String
  )

  fun updateChallenge(
    updateChallengeInput: UpdateChallengeInput,
    onSuccess: () -> Unit,
    onError: (TwilioVerifyException) -> Unit
  )

  fun getChallenge(
    challengeSid: String,
    factorSid: String,
    onSuccess: (Challenge) -> Unit,
    onError: (TwilioVerifyException) -> Unit
  )
}

object TwilioVerifyProvider {
  fun instance(applicationContext: Context, url: String): TwilioVerifyAdapter {
    return TwilioVerifyKotlinProvider.getInstance(applicationContext, url)
  }
}

