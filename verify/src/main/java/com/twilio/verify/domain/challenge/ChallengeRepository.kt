/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor

internal class ChallengeRepository(
  private val apiClient: ChallengeAPIClient,
  private val challengeMapper: ChallengeMapper = ChallengeMapper()
) : ChallengeProvider {

  override fun get(
    sid: String,
    factor: Factor,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    apiClient.get(sid, factor, { response ->
      try {
        val challenge = challengeMapper.fromApi(response)
            .also {
              if (it.factorSid != factor.sid) {
                throw TwilioVerifyException(
                    IllegalArgumentException("Wrong factor for challenge"), InputError
                )
              }
              toFactorChallenge(it).factor = factor
            }
        success(challenge)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }, error)
  }

  override fun update(
    challenge: Challenge,
    authPayload: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      if (challenge.status != Pending) {
        throw TwilioVerifyException(
            IllegalArgumentException("Responded or expired challenge can not be updated"),
            InputError
        )
      }
      toFactorChallenge(challenge).let { factorChallenge ->
        apiClient.update(factorChallenge, authPayload, {
          get(factorChallenge, success, error)
        }, error)
      }
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  private fun toFactorChallenge(challenge: Challenge) =
    (challenge as? FactorChallenge) ?: throw TwilioVerifyException(
        IllegalArgumentException("Invalid challenge"), InputError
    )

  private fun get(
    factorChallenge: FactorChallenge,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorChallenge.factor?.let {
      get(factorChallenge.sid, it, success, error)
    } ?: throw TwilioVerifyException(
        IllegalArgumentException("Invalid factor"), InputError
    )
  }
}