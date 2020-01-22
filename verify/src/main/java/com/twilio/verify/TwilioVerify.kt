/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import com.twilio.verify.domain.TwilioVerifyManager
import com.twilio.verify.domain.factor.FactorFacade
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

    @Throws(TwilioVerifyException::class)
    fun build(): TwilioVerify {
      val factorFacade = FactorFacade.Builder()
          .context(context)
          .authorization(authorization)
          .networkProvider(networkProvider)
          .build()
      return TwilioVerifyManager(factorFacade)
    }
  }
}