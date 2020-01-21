/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.domain.TwilioVerifyManager
import com.twilio.verify.domain.factor.FactorRepository
import com.twilio.verify.domain.factor.PushFactory
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkProvider

interface TwilioVerify {
  fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  class Builder(
    private var context: Context,
    private var authorization: Authorization
  ) {
    private var networkProvider: NetworkProvider = NetworkAdapter()
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networkProvider = networkProvider }

    fun build(): TwilioVerify {
      val repository = FactorRepository(
          context, FactorAPIClient(networkProvider, context, authorization)
      )
      return TwilioVerifyManager(PushFactory(repository))
    }
  }
}