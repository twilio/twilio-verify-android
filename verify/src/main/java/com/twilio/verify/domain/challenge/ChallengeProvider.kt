/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor

internal interface ChallengeProvider {
  fun get(
    sid: String,
    factor: Factor,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun update(
    challenge: Challenge,
    authPayload: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getAll(
      factor: Factor,
      status: ChallengeStatus?,
      pageSize: Int,
      pageToken: String?,
      success: (ChallengeList) -> Unit,
      error: (TwilioVerifyException) -> Unit
  )
}