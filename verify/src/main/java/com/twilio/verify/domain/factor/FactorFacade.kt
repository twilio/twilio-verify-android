/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.Authentication
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.StorageException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.UpdateFactorInput
import com.twilio.verify.models.UpdatePushFactorInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.NetworkProvider

internal class FactorFacade(
  private val pushFactory: PushFactory,
  private val factorProvider: FactorProvider
) {
  fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    when (factorInput) {
      is PushFactorInput -> {
        pushFactory.create(
            factorInput.jwt, factorInput.friendlyName, factorInput.pushToken,
            factorInput.serviceSid, factorInput.identity, success, error
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
            verifyFactorInput.sid, success, error
        )
      }
    }
  }

  fun updateFactor(
    updateFactorInput: UpdateFactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    when (updateFactorInput) {
      is UpdatePushFactorInput -> {
        pushFactory.update(updateFactorInput.sid, updateFactorInput.pushToken, success, error)
      }
    }
  }

  fun getFactorBySid(
    factorSid: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      factorProvider.get(factorSid)
          ?.let { success(it) } ?: throw TwilioVerifyException(
          StorageException("Factor not found"), StorageError
      )
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  fun getFactorByServiceSid(
    serviceSid: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      factorProvider.getAll().find { it.serviceSid == serviceSid }
          ?.let { success(it) } ?: throw TwilioVerifyException(
          StorageException("Factor not found"), StorageError
      )
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  fun getAllFactors(
    success: (List<Factor>) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      success(factorProvider.getAll())
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  fun deleteFactor(
    factorSid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      pushFactory.delete(factorSid, success, error)
    } catch (e: TwilioVerifyException) {
      error(e)
    }
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var auth: Authentication
    private lateinit var networking: NetworkProvider
    private lateinit var keyStore: KeyStorage
    private lateinit var url: String
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun authentication(authentication: Authentication) =
      apply { this.auth = authentication }

    fun keyStorage(keyStorage: KeyStorage) =
      apply { this.keyStore = keyStorage }

    fun baseUrl(url: String) = apply { this.url = url }

    @Throws(TwilioVerifyException::class)
    fun build(): FactorFacade {
      if (!this::appContext.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for context"), InitializationError
        )
      }
      if (!this::auth.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for authentication"), InitializationError
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
      if (!this::url.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for base url"),
            InitializationError
        )
      }
      val factorAPIClient =
        FactorAPIClient(networking, appContext, auth, url)
      val repository = FactorRepository(appContext, factorAPIClient)
      val pushFactory = PushFactory(repository, keyStore, appContext)
      return FactorFacade(pushFactory, repository)
    }
  }
}