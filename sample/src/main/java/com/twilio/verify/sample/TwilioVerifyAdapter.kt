/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.models.UpdateChallengePayload
import com.twilio.verify.models.VerifyFactorPayload
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.networking.SampleBackendAPIClient

interface TwilioVerifyAdapter {
  fun createFactor(
    createFactorData: CreateFactorData,
    sampleBackendAPIClient: SampleBackendAPIClient,
    success: (Factor) -> Unit,
    error: (Throwable) -> Unit
  )

  fun verifyFactor(
    verifyFactorPayload: VerifyFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun showChallenge(
    challengeSid: String,
    factorSid: String
  )

  fun updateChallenge(
    updateChallengePayload: UpdateChallengePayload,
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
    challengeListPayload: ChallengeListPayload,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun updatePushToken(token: String)
}
