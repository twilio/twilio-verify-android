/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.domain.challenge

import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import com.twilio.verify.models.ChallengeListOrder
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
          .also { challenge ->
            if (challenge.factorSid != factor.sid) {
              throw TwilioVerifyException(
                IllegalArgumentException("Wrong factor for challenge").also { Logger.log(Level.Error, it.toString(), it) }, InputError
              )
            }
            toFactorChallenge(challenge).factor = factor
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
          IllegalArgumentException("Invalid factor").also { Logger.log(Level.Error, it.toString(), it) }, InputError
        )
      )
    }
    try {
      if (challenge.status != Pending) {
        throw TwilioVerifyException(
          IllegalArgumentException("Responded or expired challenge can not be updated").also { Logger.log(Level.Error, it.toString(), it) },
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
    order: ChallengeListOrder,
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
    apiClient.getAll(factor, status?.value, pageSize, order, pageToken, ::toResponse, error)
  }

  private fun toFactorChallenge(challenge: Challenge) =
    (challenge as? FactorChallenge) ?: throw TwilioVerifyException(
      IllegalArgumentException("Invalid challenge").also { Logger.log(Level.Error, it.toString(), it) }, InputError
    )
}
