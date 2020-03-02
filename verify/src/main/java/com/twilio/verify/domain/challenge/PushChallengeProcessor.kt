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
      onSuccess()
    }
  }

  private fun generateSignature(
    challenge: FactorChallenge,
    status: ChallengeStatus
  ): String {
    val factor = challenge.factor as? PushFactor ?: throw TwilioVerifyException(
        IllegalArgumentException("Wrong factor for challenge"), InputError
    )
    val keyPairAlias = factor.keyPairAlias ?: throw TwilioVerifyException(
        IllegalStateException("Key pair not set"), KeyStorageError
    )
    val payload = "${factor.accountSid}${factor.serviceSid}${factor.entitySid}${factor.sid}" +
        "${challenge.sid}${challenge.createdDate}${challenge.updatedDate}$status" +
        "${challenge.details}${challenge.hiddenDetails}"
    return keyStorage.sign(keyPairAlias, payload)
  }
}