/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.UpdateChallengeInput
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkProvider

internal class ChallengeFacade(
  private val pushChallengeProcessor: PushChallengeProcessor,
  private val factorFacade: FactorFacade
) {
  fun getChallenge(
    sid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.getFactor(factorSid, { factor ->
      when (factor) {
        is PushFactor -> pushChallengeProcessor.get(sid, factor, success, error)
      }
    }, error)
  }

  fun updateChallenge(
    updateChallengeInput: UpdateChallengeInput,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.getFactor(updateChallengeInput.factorSid, { factor ->
      when (factor) {
        is PushFactor -> updatePushChallenge(updateChallengeInput, factor, success, error)
      }
    }, error)
  }

  private fun updatePushChallenge(
    updateChallengeInput: UpdateChallengeInput,
    factor: PushFactor,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val status = (updateChallengeInput as? UpdatePushChallengeInput)?.status
          ?: throw TwilioVerifyException(
              IllegalArgumentException(
                  "Invalid update challenge input for factor ${factor.type}"
              ), InputError
          )
      pushChallengeProcessor.update(
          updateChallengeInput.challengeSid, factor, status, success, error
      )
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var auth: Authorization
    private lateinit var networking: NetworkProvider
    private lateinit var keyStore: KeyStorage
    private lateinit var factorProvider: FactorFacade
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun authorization(authorization: Authorization) =
      apply { this.auth = authorization }

    fun keyStorage(keyStorage: KeyStorage) =
      apply { this.keyStore = keyStorage }

    fun factorFacade(factorFacade: FactorFacade) =
      apply { this.factorProvider = factorFacade }

    @Throws(TwilioVerifyException::class)
    fun build(): ChallengeFacade {
      if (!this::appContext.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for context"), InitializationError
        )
      }
      if (!this::auth.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for authorization"), InitializationError
        )
      }
      if (!this::networking.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for network provider"),
            InitializationError
        )
      }
      if (!this::keyStore.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for key storage"),
            InitializationError
        )
      }
      if (!this::factorProvider.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for key storage"),
            InitializationError
        )
      }
      val challengeAPIClient = ChallengeAPIClient(networking, appContext, auth)
      val repository = ChallengeRepository(challengeAPIClient)
      val pushChallengeProcessor = PushChallengeProcessor(repository, keyStore)
      return ChallengeFacade(pushChallengeProcessor, factorProvider)
    }
  }
}