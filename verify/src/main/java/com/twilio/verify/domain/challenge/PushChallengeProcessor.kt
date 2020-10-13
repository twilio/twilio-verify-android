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

import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.INPUT_ERROR
import com.twilio.verify.TwilioVerifyException.ErrorCode.KEY_STORAGE_ERROR
import com.twilio.verify.data.getSignerTemplate
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import org.json.JSONObject

internal class PushChallengeProcessor(
  private val challengeProvider: ChallengeProvider,
  private val jwtGenerator: JwtGenerator
) {

  fun get(
    sid: String,
    factor: PushFactor,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    challengeProvider.get(
      sid, factor,
      { challenge ->
        success(challenge)
      },
      { exception ->
        error(exception)
      }
    )
  }

  fun update(
    sid: String,
    factor: PushFactor,
    status: ChallengeStatus,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun updateChallenge(challenge: Challenge) {
      try {
        val factorChallenge = challenge as? FactorChallenge ?: throw TwilioVerifyException(
          IllegalArgumentException("Invalid challenge"),
          INPUT_ERROR
        )
        if (challenge.factor == null || challenge.factor !is PushFactor ||
          challenge.factor?.sid != factor.sid
        ) {
          throw TwilioVerifyException(
            IllegalArgumentException("Wrong factor for challenge"), INPUT_ERROR
          )
        }
        val keyPairAlias = factor.keyPairAlias?.takeIf { it.isNotBlank() }
          ?: throw TwilioVerifyException(
            IllegalStateException("Key pair not set"), KEY_STORAGE_ERROR
          )
        val signatureFields = factorChallenge.signatureFields?.takeIf { it.isNotEmpty() }
          ?: throw TwilioVerifyException(
            IllegalStateException("Signature fields not set"), INPUT_ERROR
          )
        val response =
          factorChallenge.response?.takeIf { it.length() > 0 } ?: throw TwilioVerifyException(
            IllegalStateException("Challenge response not set"), INPUT_ERROR
          )
        val authPayload =
          generateSignature(
            signatureFields, response, status, getSignerTemplate(keyPairAlias, true)
          )
        challengeProvider.update(
          challenge, authPayload,
          { updatedChallenge ->
            updatedChallenge.takeIf { updatedChallenge.status == status }
              ?.run {
                success()
              } ?: error(
              TwilioVerifyException(
                IllegalStateException("Challenge was not updated"),
                INPUT_ERROR
              )
            )
          },
          error
        )
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }

    get(sid, factor, ::updateChallenge, error)
  }

  private fun generateSignature(
    signatureFields: List<String>,
    response: JSONObject,
    status: ChallengeStatus,
    signerTemplate: SignerTemplate
  ): String {
    try {
      val payload = JSONObject().apply {
        signatureFields.forEach {
          put(it, response[it])
        }
        put(statusKey, status.value)
      }
      return jwtGenerator.generateJWT(signerTemplate, JSONObject(), payload)
    } catch (e: Exception) {
      throw TwilioVerifyException(e, INPUT_ERROR)
    }
  }
}
