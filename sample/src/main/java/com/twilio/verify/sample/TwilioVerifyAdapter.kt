/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListInput
import com.twilio.verify.models.Factor
import com.twilio.verify.models.UpdateChallengeInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.sample.kotlin.TwilioVerifyKotlinProvider
import com.twilio.verify.sample.model.CreateFactorData

interface TwilioVerifyAdapter {
  fun createFactor(
    createFactorData: CreateFactorData,
    success: (Factor) -> Unit,
    error: (Exception) -> Unit
  )

  fun verifyFactor(
    verifyFactorInput: VerifyFactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun showChallenge(
    challengeSid: String,
    factorSid: String
  )

  fun updateChallenge(
    updateChallengeInput: UpdateChallengeInput,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getChallenge(
    challengeSid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getFactors(
    success: (List<Factor>) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun deleteFactor(
    factorSid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getAllChallenges(
    challengeListInput: ChallengeListInput,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )
}

