/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InitializationError
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkProvider

internal class FactorFacade(private val pushFactory: PushFactory) {

  class Builder {
    private lateinit var appContext: Context
    private lateinit var auth: Authorization
    private lateinit var networking: NetworkProvider
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun authorization(authorization: Authorization) =
      apply { this.auth = authorization }

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
            IllegalArgumentException("Illegal value for network provider"), InitializationError
        )
      }
      val factorAPIClient = FactorAPIClient(networking, appContext, auth)
      val repository = FactorRepository(appContext, factorAPIClient)
      val pushFactory = PushFactory(repository)
      return FactorFacade(pushFactory)
    }
  }
}