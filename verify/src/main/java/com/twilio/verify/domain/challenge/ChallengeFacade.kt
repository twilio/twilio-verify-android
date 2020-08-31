/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.models.UpdateChallengePayload
import com.twilio.verify.models.UpdatePushChallengePayload
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.threading.execute

internal class ChallengeFacade(
  private val pushChallengeProcessor: PushChallengeProcessor,
  private val factorFacade: FactorFacade,
  private val repository: ChallengeProvider
) {
  fun getChallenge(
    sid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      factorFacade.getFactor(
        factorSid,
        { factor ->
          when (factor) {
            is PushFactor -> pushChallengeProcessor.get(sid, factor, onSuccess, onError)
          }
        },
        onError
      )
    }
  }

  fun updateChallenge(
    updateChallengePayload: UpdateChallengePayload,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      factorFacade.getFactor(
        updateChallengePayload.factorSid,
        { factor ->
          when (factor) {
            is PushFactor -> updatePushChallenge(updateChallengePayload, factor, onSuccess, onError)
          }
        },
        onError
      )
    }
  }

  fun getAllChallenges(
    challengeListPayload: ChallengeListPayload,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.getFactor(
      challengeListPayload.factorSid,
      { factor ->
        execute(success, error) { onSuccess, onError ->
          repository.getAll(
            factor, challengeListPayload.status, challengeListPayload.pageSize,
            challengeListPayload.pageToken,
            { list ->
              onSuccess(list)
            },
            { exception ->
              onError(exception)
            }
          )
        }
      },
      error
    )
  }

  private fun updatePushChallenge(
    updateChallengePayload: UpdateChallengePayload,
    factor: PushFactor,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val status = (updateChallengePayload as? UpdatePushChallengePayload)?.status
        ?: throw TwilioVerifyException(
          IllegalArgumentException(
            "Invalid update challenge input for factor ${factor.type}"
          ),
          InputError
        )
      pushChallengeProcessor.update(
        updateChallengePayload.challengeSid, factor, status, success, error
      )
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var networking: NetworkProvider
    private lateinit var generator: JwtGenerator
    private lateinit var factorProvider: FactorFacade
    private lateinit var url: String
    private lateinit var authentication: Authentication
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun jwtGenerator(jwtGenerator: JwtGenerator) =
      apply { this.generator = jwtGenerator }

    fun factorFacade(factorFacade: FactorFacade) =
      apply { this.factorProvider = factorFacade }

    fun baseUrl(url: String) = apply { this.url = url }

    fun setAuthentication(authentication: Authentication) =
      apply { this.authentication = authentication }

    @Throws(TwilioVerifyException::class)
    fun build(): ChallengeFacade {
      if (!this::appContext.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for context"), InitializationError
        )
      }
      if (!this::networking.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for network provider"),
          InitializationError
        )
      }
      if (!this::generator.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for JWT generator"),
          InitializationError
        )
      }
      if (!this::factorProvider.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for factor provider"),
          InitializationError
        )
      }
      if (!this::url.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for base url"),
          InitializationError
        )
      }
      if (!this::authentication.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for authentication"),
          InitializationError
        )
      }
      val challengeAPIClient = ChallengeAPIClient(networking, appContext, authentication, url)
      val repository = ChallengeRepository(challengeAPIClient)
      val pushChallengeProcessor = PushChallengeProcessor(repository, generator)
      return ChallengeFacade(pushChallengeProcessor, factorProvider, repository)
    }
  }
}
