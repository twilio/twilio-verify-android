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
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.AlreadyUpdatedChallengeException
import com.twilio.verify.ExpiredChallengeException
import com.twilio.verify.InvalidChallengeException
import com.twilio.verify.NotUpdatedChallengeException
import com.twilio.verify.SignatureFieldsException
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.WrongFactorException
import com.twilio.verify.data.getSignerTemplate
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Expired
import com.twilio.verify.models.ChallengeStatus.Pending
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
    Logger.log(Level.Info, "Getting challenge $sid with factor ${factor.sid}")
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
          InvalidChallengeException.also { Logger.log(Level.Error, it.toString(), it) },
          InputError
        )
        if (challenge.factor == null || challenge.factor !is PushFactor ||
          challenge.factor?.sid != factor.sid
        ) {
          throw TwilioVerifyException(
            WrongFactorException.also { Logger.log(Level.Error, it.toString(), it) }, InputError
          )
        }
        if (challenge.status == Expired) {
          throw TwilioVerifyException(
            ExpiredChallengeException.also { Logger.log(Level.Error, it.toString(), it) },
            InputError
          )
        }
        if (challenge.status != Pending) {
          throw TwilioVerifyException(
            AlreadyUpdatedChallengeException.also { Logger.log(Level.Error, it.toString(), it) },
            InputError
          )
        }
        val keyPairAlias = factor.keyPairAlias?.takeIf { it.isNotBlank() }
          ?: throw TwilioVerifyException(
            IllegalStateException("Key pair not set").also { Logger.log(Level.Error, it.toString(), it) }, KeyStorageError
          )
        val signatureFields = factorChallenge.signatureFields?.takeIf { it.isNotEmpty() }
          ?: throw TwilioVerifyException(
            SignatureFieldsException.also { Logger.log(Level.Error, it.toString(), it) }, InputError
          )
        val response =
          factorChallenge.response?.takeIf { it.length() > 0 } ?: throw TwilioVerifyException(
            SignatureFieldsException.also { Logger.log(Level.Error, it.toString(), it) }, InputError
          )
        val authPayload =
          generateSignature(
            signatureFields, response, status, getSignerTemplate(keyPairAlias, true)
          )
        Logger.log(Level.Debug, "Update challenge with auth payload $authPayload")
        challengeProvider.update(
          challenge, authPayload,
          { updatedChallenge ->
            updatedChallenge.takeIf { updatedChallenge.status == status }
              ?.run {
                success()
              } ?: error(
              TwilioVerifyException(
                NotUpdatedChallengeException.also { Logger.log(Level.Error, it.toString(), it) },
                InputError
              )
            )
          },
          error
        )
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
    Logger.log(Level.Info, "Updating challenge $sid with factor ${factor.sid} to new status ${status.value}")
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
      Logger.log(Level.Debug, "Update challenge with payload $payload")
      return jwtGenerator.generateJWT(signerTemplate, JSONObject(), payload)
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, InputError)
    }
  }
}
