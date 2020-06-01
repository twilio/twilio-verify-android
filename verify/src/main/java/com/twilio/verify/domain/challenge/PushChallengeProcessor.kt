/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.data.getSignerTemplate
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.threading.execute
import org.json.JSONObject

internal const val signatureKey = "signature"

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
    execute(success, error) { onSuccess, onError ->
      challengeProvider.get(sid, factor, { challenge ->
        onSuccess(challenge)
      }, { exception ->
        onError(exception)
      })
    }
  }

  fun update(
    sid: String,
    factor: PushFactor,
    status: ChallengeStatus,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      fun updateChallenge(challenge: Challenge) {
        try {
          val factorChallenge = challenge as? FactorChallenge ?: throw TwilioVerifyException(
              IllegalArgumentException("Invalid challenge"),
              InputError
          )
          if (challenge.factor == null || challenge.factor !is PushFactor ||
              challenge.factor?.sid != factor.sid
          ) {
            throw TwilioVerifyException(
                IllegalArgumentException("Wrong factor for challenge"), InputError
            )
          }
          val keyPairAlias = factor.keyPairAlias?.takeIf { it.isNotBlank() }
              ?: throw TwilioVerifyException(
                  IllegalStateException("Key pair not set"), KeyStorageError
              )
          val signatureFields = factorChallenge.signatureFields?.takeIf { it.isNotEmpty() }
              ?: throw TwilioVerifyException(
                  IllegalStateException("Signature fields not set"), InputError
              )
          val response =
            factorChallenge.response?.takeIf { it.length() > 0 } ?: throw TwilioVerifyException(
                IllegalStateException("Challenge response not set"), InputError
            )
          val authPayload =
            generateSignature(
                signatureFields, response, status, getSignerTemplate(keyPairAlias, true)
            )
          challengeProvider.update(challenge, authPayload, { updatedChallenge ->
            updatedChallenge.takeIf { updatedChallenge.status == status }
                ?.run {
                  onSuccess()
                } ?: onError(
                TwilioVerifyException(
                    IllegalStateException("Challenge was not updated"),
                    InputError
                )
            )
          }, onError)
        } catch (e: TwilioVerifyException) {
          onError(e)
        }
      }

      get(sid, factor, ::updateChallenge, error)
    }
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
      throw TwilioVerifyException(e, InputError)
    }
  }
}