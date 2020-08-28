/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.security.storage.encryptedPreferences
import com.twilio.verify.ENC_SUFFIX
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.VERIFY_SUFFIX
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.Storage
import com.twilio.verify.data.StorageException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorPayload
import com.twilio.verify.models.PushFactorPayload
import com.twilio.verify.models.UpdateFactorPayload
import com.twilio.verify.models.UpdatePushFactorPayload
import com.twilio.verify.models.VerifyFactorPayload
import com.twilio.verify.models.VerifyPushFactorPayload
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.threading.execute

internal class FactorFacade(
  private val pushFactory: PushFactory,
  private val factorProvider: FactorProvider
) {
  fun createFactor(
    factorPayload: FactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      when (factorPayload) {
        is PushFactorPayload -> {
          pushFactory.create(
              factorPayload.accessToken, factorPayload.friendlyName, factorPayload.pushToken,
              factorPayload.serviceSid, factorPayload.identity, onSuccess, onError
          )
        }
      }
    }
  }

  fun verifyFactor(
    verifyFactorPayload: VerifyFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      when (verifyFactorPayload) {
        is VerifyPushFactorPayload -> {
          pushFactory.verify(
              verifyFactorPayload.sid, onSuccess, onError
          )
        }
      }
    }
  }

  fun updateFactor(
    updateFactorPayload: UpdateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      when (updateFactorPayload) {
        is UpdatePushFactorPayload -> {
          pushFactory.update(
              updateFactorPayload.sid, updateFactorPayload.pushToken, onSuccess, onError
          )
        }
      }
    }
  }

  fun getFactor(
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
      factorProvider.getAll()
          .find { it.serviceSid == serviceSid }
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
    execute(success, error) { onSuccess, onError ->
      try {
        pushFactory.delete(factorSid, onSuccess, onError)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var networking: NetworkProvider
    private lateinit var keyStore: KeyStorage
    private lateinit var url: String
    private lateinit var authentication: Authentication
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun keyStorage(keyStorage: KeyStorage) =
      apply { this.keyStore = keyStorage }

    fun baseUrl(url: String) = apply { this.url = url }

    fun setAuthentication(authentication: Authentication) =
      apply { this.authentication = authentication }

    @Throws(TwilioVerifyException::class)
    fun build(): FactorFacade {
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
      if (!this::authentication.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for authentication"),
            InitializationError
        )
      }
      val storageName = "${appContext.packageName}.$VERIFY_SUFFIX"
      val factorAPIClient = FactorAPIClient(networking, appContext, authentication, url)
      val sharedPreferences = appContext.getSharedPreferences(storageName, Context.MODE_PRIVATE)
      val encryptedSharedPreferences =
        appContext.getSharedPreferences("$storageName.$ENC_SUFFIX", Context.MODE_PRIVATE)
      val encryptedStorage = encryptedPreferences(storageName, encryptedSharedPreferences)
      val factorMigrations = FactorMigrations(sharedPreferences)
      val storage = Storage(sharedPreferences, encryptedStorage, factorMigrations.migrations())
      val repository = FactorRepository(factorAPIClient, storage)
      val pushFactory = PushFactory(repository, keyStore, appContext)
      return FactorFacade(pushFactory, repository)
    }
  }
}