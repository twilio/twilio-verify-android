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
import com.twilio.verify.logger.DefaultLoggerService
import com.twilio.verify.logger.LogLevel
import com.twilio.verify.logger.Logger
import com.twilio.verify.logger.LoggerService
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

/**
 * Describes the available operations to proccess Factors and Challenges
 */
interface TwilioVerify {
  /**
   * Creates a **Factor** from a **FactorPayload**
   * @param factorPayload Describes Information needed to create a Factor
   * @param success Block to be called when the operation succeeds, returns the created Factor
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun createFactor(
    factorPayload: FactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Verifies a **Factor** from a **VerifyFactorPayload**
   * @param factorPayload Describes the information needed to verify a factor
   * @param success Block to be called when the operation succeeds, returns the verified Factor
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun verifyFactor(
    verifyFactorPayload: VerifyFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Updates a **Factor** from a **UpdateFactorPayload**
   * @param updateFactorPayload Describes the information needed to update a factor
   * @param success Block to be called when the operation succeeds, returns the updated Factor
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun updateFactor(
    updateFactorPayload: UpdateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Gets all **Factors** created by the app
   * @param success Block to be called when the operation succeeds, returns a List of Factor
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun getAllFactors(
    success: (List<Factor>) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Gets a **Challenge** with the given challenge sid and factor sid
   * @param challengeSid Sid of the Challenge requested
   * @param factorSid Sid of the Factor to which the Challenge corresponds
   * @param success Block to be called when the operation succeeds, returns the requested Challenge
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun getChallenge(
    challengeSid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Gets all Challenges associated to a **Factor** with the given **ChallengeListPayload**
   * @param challengeListPayload Describes the information needed to fetch all the **Challenges**
   * @param success Block to be called when the operation succeeds, returns a ChallengeList
   * which contains the Challenges and the metadata associated to the request
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun getAllChallenges(
    challengeListPayload: ChallengeListPayload,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Updates a **Challenge** from a **UpdateChallengePayload**
   * @param updateChallengePayload Describes the information needed to update a challenge
   * @param success Block to be called when the operation succeeds
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun updateChallenge(
    updateChallengePayload: UpdateChallengePayload,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Deletes a **Factor** with the given **sid**
   * @param factorSid Sid of the **Factor** to be deleted
   * @param success Block to be called when the operation succeeds
   * @param error Block to be called when the operation fails with the cause of failure
   */
  fun deleteFactor(
    factorSid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  /**
   * Builder class that builds an instance of TwilioVerifyManager, which handles all the operations
   * regarding Factors and Challenges
   */
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
    private var logLevel: LogLevel = LogLevel.OFF
    private var loggerService: LoggerService? = null

    /**
     * @param networkProvider
     */
    fun networkProvider(networkProvider: NetworkProvider) =
      apply { this.networkProvider = networkProvider }

    internal fun baseUrl(baseUrl: String) = apply {
      this.baseUrl = baseUrl
    }

    fun logLevel(logLevel: LogLevel) =
      apply { this.logLevel = logLevel }

    fun loggingService(loggerService: LoggerService) =
      apply { this.loggerService = loggerService }

    /**
     * Builds an instance of TwilioVerifyManager
     * @throws TwilioVerifyException When building TwilioVerifyManager fails
     * @return Instance of twilioVerifyManager
     */
    @Throws(TwilioVerifyException::class)
    fun build(): TwilioVerify {
      loggerService?.let { Logger.addService(it) } ?: run {
        Logger.addService(DefaultLoggerService(logLevel))
      }
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
internal const val ENC_SUFFIX = "enc"
