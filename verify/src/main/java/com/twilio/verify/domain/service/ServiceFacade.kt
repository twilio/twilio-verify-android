package com.twilio.verify.domain.service

import android.content.Context
import com.twilio.verify.networking.AuthenticationProvider
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.api.ServiceAPIClient
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Service
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.threading.execute

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal class ServiceFacade(
  private val serviceProvider: ServiceProvider,
  private val factorFacade: FactorFacade
) {

  fun getService(
    serviceSid: String,
    success: (Service) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.getFactorByServiceSid(serviceSid, { factor ->
      execute(success, error) { onSuccess, onError ->
        try {
          serviceProvider.get(serviceSid, factor, onSuccess, onError)
        } catch (e: TwilioVerifyException) {
          onError(e)
        }
      }
    }, error)
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var networking: NetworkProvider
    private lateinit var url: String
    private lateinit var factorFacade: FactorFacade

    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun baseUrl(url: String) = apply { this.url = url }

    fun setFactorFacade(factorFacade: FactorFacade) =
      apply { this.factorFacade = factorFacade }

    @Throws(TwilioVerifyException::class)
    fun build(): ServiceFacade {
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
      if (!this::url.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for base url"),
            InitializationError
        )
      }

      if (!this::factorFacade.isInitialized) {
        throw TwilioVerifyException(
            IllegalArgumentException("Illegal value for factor facade"),
            InitializationError
        )
      }
      val serviceAPIClient =
        ServiceAPIClient(networking, appContext,
            AuthenticationProvider(), url)
      return ServiceFacade(ServiceRepository(serviceAPIClient), factorFacade)
    }
  }
}