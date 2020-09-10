/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import org.json.JSONObject

internal class ChallengeRepository(
  private val apiClient: ChallengeAPIClient,
  private val challengeMapper: ChallengeMapper = ChallengeMapper(),
  private val challengeListMapper: ChallengeListMapper = ChallengeListMapper()
) : ChallengeProvider {

  override fun get(
    sid: String,
    factor: Factor,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun toChallenge(
      response: JSONObject,
      signatureFieldsHeader: String?
    ) {
      try {
        val challenge = challengeMapper.fromApi(response, signatureFieldsHeader)
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
    }
    apiClient.get(sid, factor, ::toChallenge, error)
  }

  override fun update(
    challenge: Challenge,
    authPayload: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun getChallenge(factorChallenge: FactorChallenge) {
      factorChallenge.factor?.let {
        get(factorChallenge.sid, it, success, error)
      } ?: error(
        TwilioVerifyException(
          IllegalArgumentException("Invalid factor"), InputError
        )
      )
    }
    try {
      if (challenge.status != Pending) {
        throw TwilioVerifyException(
          IllegalArgumentException("Responded or expired challenge can not be updated"),
          InputError
        )
      }
      toFactorChallenge(challenge).let { factorChallenge ->
        apiClient.update(factorChallenge, authPayload, { getChallenge(factorChallenge) }, error)
      }
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  override fun getAll(
    factor: Factor,
    status: ChallengeStatus?,
    pageSize: Int,
    pageToken: String?,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun toResponse(response: JSONObject) {
      try {
        val challengeList = challengeListMapper.fromApi(response)
        success(challengeList)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
    apiClient.getAll(factor, status?.value, pageSize, pageToken, ::toResponse, error)
  }

  private fun toFactorChallenge(challenge: Challenge) =
    (challenge as? FactorChallenge) ?: throw TwilioVerifyException(
      IllegalArgumentException("Invalid challenge"), InputError
    )
}
