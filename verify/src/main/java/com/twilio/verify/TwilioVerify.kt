/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.KeyStoreAdapter
import com.twilio.verify.domain.TwilioVerifyManager
import com.twilio.verify.domain.challenge.ChallengeFacade
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.UpdateChallengeInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkProvider

interface TwilioVerify {
  fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun verifyFactor(
    verifyFactorInput: VerifyFactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getChallenge(
    challengeSid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun updateChallenge(
    updateChallengeInput: UpdateChallengeInput,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  class Builder(
    private var context: Context,
    private var authorization: Authorization
  ) {
    private var keyStorage: KeyStorage = KeyStoreAdapter()
    private var networkProvider: NetworkProvider = NetworkAdapter()
    private var baseUrl: String = BuildConfig.BASE_URL
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networkProvider = networkProvider }

    internal fun baseUrl(baseUrl: String) = apply {
      this.baseUrl = baseUrl
    }

    @Throws(TwilioVerifyException::class)
    fun build(): TwilioVerify {
      val factorFacade = FactorFacade.Builder()
          .context(context)
          .authorization(authorization)
          .networkProvider(networkProvider)
          .keyStorage(keyStorage)
          .baseUrl(baseUrl)
          .build()
      val challengeFacade = ChallengeFacade.Builder()
          .context(context)
          .authorization(authorization)
          .networkProvider(networkProvider)
          .keyStorage(keyStorage)
          .factorFacade(factorFacade)
          .baseUrl(baseUrl)
          .build()
      return TwilioVerifyManager(factorFacade, challengeFacade)
    }
  }
}