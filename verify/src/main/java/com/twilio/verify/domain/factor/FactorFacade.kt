/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkProvider

internal class FactorFacade(private val pushFactory: PushFactory) {

  fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    when (factorInput) {
      is PushFactorInput -> {
        pushFactory.create(
            factorInput.jwt, factorInput.friendlyName, factorInput.pushToken, success, error
        )
      }
    }
  }

  fun verifyFactor(
    verifyFactorInput: VerifyFactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    when (verifyFactorInput) {
      is VerifyPushFactorInput -> {
        pushFactory.verify(
            verifyFactorInput.sid, verifyFactorInput.verificationCode, success, error
        )
      }
    }
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var auth: Authorization
    private lateinit var networking: NetworkProvider
    private lateinit var keyStore: KeyStorage
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun authorization(authorization: Authorization) =
      apply { this.auth = authorization }

    fun keyStorage(keyStorage: KeyStorage) =
      apply { this.keyStore = keyStorage }

    @Throws(TwilioVerifyException::class)
    fun build(): FactorFacade {
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
      val factorAPIClient = FactorAPIClient(networking, appContext, auth)
      val repository = FactorRepository(appContext, factorAPIClient)
      val pushFactory = PushFactory(repository, keyStore)
      return FactorFacade(pushFactory)
    }
  }
}