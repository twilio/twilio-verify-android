/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import com.twilio.verify.data.DateAdapter
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.KeyStoreAdapter
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.data.jwt.JwtSigner
import com.twilio.verify.domain.TwilioVerifyManager
import com.twilio.verify.domain.challenge.ChallengeFacade
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.domain.service.ServiceFacade
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorPayload
import com.twilio.verify.models.UpdateChallengePayload
import com.twilio.verify.models.UpdateFactorPayload
import com.twilio.verify.models.VerifyFactorPayload
import com.twilio.verify.networking.AuthenticationProvider
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkProvider

interface TwilioVerify {
  fun createFactor(
    factorPayload: FactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun verifyFactor(
    verifyFactorPayload: VerifyFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun updateFactor(
    updateFactorPayload: UpdateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getAllFactors(
    success: (List<Factor>) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getChallenge(
    challengeSid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun getAllChallenges(
    challengeListPayload: ChallengeListPayload,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun updateChallenge(
    updateChallengePayload: UpdateChallengePayload,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun deleteFactor(
    factorSid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  class Builder(
    private var context: Context
  ) {
    private var keyStorage: KeyStorage = KeyStoreAdapter()
    private var networkProvider: NetworkProvider = NetworkAdapter()
    private var baseUrl: String = BuildConfig.BASE_URL
    private var jwtGenerator: JwtGenerator = JwtGenerator(JwtSigner(keyStorage))
    private var authentication =
      AuthenticationProvider(
          jwtGenerator,
          DateAdapter(storagePreferences(context))
      )

    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networkProvider = networkProvider }

    internal fun baseUrl(baseUrl: String) = apply {
      this.baseUrl = baseUrl
    }

    @Throws(TwilioVerifyException::class)
    fun build(): TwilioVerify {
      val factorFacade = FactorFacade.Builder()
          .context(context)
          .networkProvider(networkProvider)
          .keyStorage(keyStorage)
          .baseUrl(baseUrl)
          .setAuthentication(authentication)
          .build()
      val challengeFacade = ChallengeFacade.Builder()
          .context(context)
          .networkProvider(networkProvider)
          .jwtGenerator(jwtGenerator)
          .factorFacade(factorFacade)
          .baseUrl(baseUrl)
          .setAuthentication(authentication)
          .build()
      val serviceFacade = ServiceFacade.Builder()
          .context(context)
          .networkProvider(networkProvider)
          .setFactorFacade(factorFacade)
          .setAuthentication(authentication)
          .baseUrl(baseUrl)
          .build()
      return TwilioVerifyManager(factorFacade, challengeFacade, serviceFacade)
    }
  }
}

internal fun storagePreferences(context: Context) =
  context.getSharedPreferences("${context.packageName}.$VERIFY_SUFFIX", Context.MODE_PRIVATE)
internal const val VERIFY_SUFFIX = "verify"