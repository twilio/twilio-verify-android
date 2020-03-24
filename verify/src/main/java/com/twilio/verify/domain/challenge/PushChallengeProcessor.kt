/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.threading.execute
import org.json.JSONObject

internal const val signatureKey = "signature"

internal class PushChallengeProcessor(
  private val challengeProvider: ChallengeProvider,
  private val keyStorage: KeyStorage
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
          val authPayload = authPayload(factorChallenge, status)
          challengeProvider.update(challenge, authPayload, { updatedChallenge ->
            updatedChallenge.takeIf { updatedChallenge.status == status }?.run {
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

  private fun authPayload(
    factorChallenge: FactorChallenge,
    status: ChallengeStatus
  ): String = JSONObject().apply {
    put(signatureKey, generateSignature(factorChallenge))
    put(statusKey, status.value)
  }.toString()

  private fun generateSignature(
    challenge: FactorChallenge
  ): String {
    val factor = challenge.factor as? PushFactor ?: throw TwilioVerifyException(
        IllegalArgumentException("Wrong factor for challenge"), InputError
    )
    val keyPairAlias = factor.keyPairAlias?.takeIf { it.isNotBlank() }
        ?: throw TwilioVerifyException(
            IllegalStateException("Key pair not set"), KeyStorageError
        )
    val payload = "${factor.accountSid}${factor.serviceSid}${challenge.entitySid}${factor.sid}" +
        "${challenge.sid}${challenge.createdDate}${challenge.updatedDate}${challenge.status.value}" +
        "${challenge.details}${challenge.hiddenDetails}"
    return keyStorage.sign(keyPairAlias, payload)
  }
}