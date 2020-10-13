/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.domain.service

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.INITIALIZATION_ERROR
import com.twilio.verify.api.ServiceAPIClient
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Service
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.threading.execute

internal class ServiceFacade(
  private val serviceProvider: ServiceProvider,
  private val factorFacade: FactorFacade
) {

  fun getService(
    serviceSid: String,
    success: (Service) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.getFactorByServiceSid(
      serviceSid,
      { factor ->
        execute(success, error) { onSuccess, onError ->
          try {
            serviceProvider.get(serviceSid, factor, onSuccess, onError)
          } catch (e: TwilioVerifyException) {
            onError(e)
          }
        }
      },
      error
    )
  }

  class Builder {
    private lateinit var appContext: Context
    private lateinit var networking: NetworkProvider
    private lateinit var url: String
    private lateinit var factorFacade: FactorFacade
    private lateinit var authentication: Authentication

    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networking = networkProvider }

    fun context(context: Context) =
      apply { this.appContext = context }

    fun baseUrl(url: String) = apply { this.url = url }

    fun setFactorFacade(factorFacade: FactorFacade) =
      apply { this.factorFacade = factorFacade }

    fun setAuthentication(authentication: Authentication) =
      apply { this.authentication = authentication }

    @Throws(TwilioVerifyException::class)
    fun build(): ServiceFacade {
      if (!this::appContext.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for context"), INITIALIZATION_ERROR
        )
      }
      if (!this::networking.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for network provider"),
          INITIALIZATION_ERROR
        )
      }
      if (!this::url.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for base url"),
          INITIALIZATION_ERROR
        )
      }
      if (!this::factorFacade.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for factor facade"),
          INITIALIZATION_ERROR
        )
      }
      if (!this::authentication.isInitialized) {
        throw TwilioVerifyException(
          IllegalArgumentException("Illegal value for authentication"),
          INITIALIZATION_ERROR
        )
      }
      val serviceAPIClient = ServiceAPIClient(networking, appContext, authentication, url)
      return ServiceFacade(ServiceRepository(serviceAPIClient), factorFacade)
    }
  }
}
