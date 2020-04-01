package com.twilio.verify.domain.service

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.api.ServiceAPIClient
import com.twilio.verify.models.Service
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.threading.execute

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal class ServiceFacade(private val serviceProvider: ServiceProvider) {

  fun getService(
    serviceSid: String,
    success: (Service) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    execute(success, error) { onSuccess, onError ->
      try {
        serviceProvider.get(serviceSid, onSuccess, onError)
      } catch (e: TwilioVerifyException) {
        onError(e)
      }
    }
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var auth: Authorization
    private lateinit var networking: NetworkProvider
    private lateinit var url: String
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun authorization(authorization: Authorization) =
      apply { this.auth = authorization }

    fun baseUrl(url: String) = apply { this.url = url }

    @Throws(TwilioVerifyException::class)
    fun build(): ServiceFacade {
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
      if (!this::url.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for base url"),
            InitializationError
        )
      }
      val serviceAPIClient =
        ServiceAPIClient(networking, appContext, auth, url)
      return ServiceFacade(ServiceRepository(serviceAPIClient))
    }
  }
}