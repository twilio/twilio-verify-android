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

package com.twilio.verify.api

import android.content.Context
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateAdapter
import com.twilio.verify.data.DateProvider
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.ChallengeListOrder
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.BasicAuthorization
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper
import com.twilio.verify.storagePreferences
import org.json.JSONObject

internal const val CHALLENGE_SID_PATH = "{ChallengeSid}"
internal const val STATUS_PARAMETER = "Status"
internal const val PAGE_SIZE_PARAMETER = "PageSize"
internal const val PAGE_TOKEN_PARAMETER = "PageToken"
internal const val ORDER_PARAMETER = "Order"
internal const val SIGNATURE_FIELDS_HEADER = "Twilio-Verify-Signature-Fields"
internal const val UPDATE_CHALLENGE_URL =
  "Services/$SERVICE_SID_PATH/Entities/$IDENTITY_PATH/Challenges/$CHALLENGE_SID_PATH"
internal const val GET_CHALLENGE_URL =
  "Services/$SERVICE_SID_PATH/Entities/$IDENTITY_PATH/Challenges/$CHALLENGE_SID_PATH"
internal const val GET_CHALLENGES_URL =
  "Services/$SERVICE_SID_PATH/Entities/$IDENTITY_PATH/Challenges"

internal const val FACTOR_SID_KEY = "FactorSid"

internal class ChallengeAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authentication: Authentication,
  private val baseUrl: String,
  dateProvider: DateProvider =
    DateAdapter(
      storagePreferences(context),
    ),
) : BaseAPIClient(dateProvider) {
  fun update(
    challenge: FactorChallenge,
    authPayload: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit,
  ) {
    fun updateChallenge(retries: Int = RETRY_TIMES) {
      try {
        val factor =
          challenge.factor ?: throw IllegalArgumentException(
            "Factor is null",
          )
        val authToken = authentication.generateJWT(factor)
        val requestHelper =
          RequestHelper(
            context,
            BasicAuthorization(AUTHENTICATION_USER, authToken),
          )
        val request =
          Request
            .Builder(
              requestHelper,
              updateChallengeURL(challenge),
            ).httpMethod(Post)
            .body(updateChallengeBody(authPayload))
            .build()
        networkProvider.execute(
          request,
          {
            success()
          },
          { exception ->
            validateException(exception, ::updateChallenge, retries, error)
          },
        )
      } catch (e: TwilioVerifyException) {
        error(e)
      } catch (e: Exception) {
        Logger.log(Level.Error, e.toString(), e)
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    updateChallenge()
  }

  fun get(
    sid: String,
    factor: Factor,
    success: (response: JSONObject, SIGNATURE_FIELDS_HEADER: String?) -> Unit,
    error: (TwilioVerifyException) -> Unit,
  ) {
    fun getChallenge(retries: Int = RETRY_TIMES) {
      try {
        val authToken = authentication.generateJWT(factor)
        val requestHelper =
          RequestHelper(context, BasicAuthorization(AUTHENTICATION_USER, authToken))
        val request =
          Request
            .Builder(
              requestHelper,
              getChallengeURL(sid, factor),
            ).httpMethod(Get)
            .build()
        networkProvider.execute(
          request,
          {
            success(
              JSONObject(it.body),
              it.headers[SIGNATURE_FIELDS_HEADER]?.first(),
            )
          },
          { exception ->
            validateException(exception, ::getChallenge, retries, error)
          },
        )
      } catch (e: TwilioVerifyException) {
        error(e)
      } catch (e: Exception) {
        Logger.log(Level.Error, e.toString(), e)
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    getChallenge()
  }

  fun getAll(
    factor: Factor,
    status: String?,
    pageSize: Int,
    order: ChallengeListOrder,
    pageToken: String?,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit,
  ) {
    fun getAllChallenges(retries: Int = RETRY_TIMES) {
      try {
        val authToken = authentication.generateJWT(factor)
        val requestHelper =
          RequestHelper(context, BasicAuthorization(AUTHENTICATION_USER, authToken))
        val queryParameters =
          mutableMapOf<String, Any>(
            PAGE_SIZE_PARAMETER to pageSize,
            FACTOR_SID_KEY to factor.sid,
            ORDER_PARAMETER to order.name.lowercase(),
          )
        status?.let {
          queryParameters.put(STATUS_PARAMETER, it)
        }
        pageToken?.let {
          queryParameters.put(PAGE_TOKEN_PARAMETER, it)
        }
        val request =
          Request
            .Builder(
              requestHelper,
              getChallengesURL(factor),
            ).httpMethod(Get)
            .query(queryParameters)
            .build()
        networkProvider.execute(
          request,
          {
            success(JSONObject(it.body))
          },
          { exception ->
            validateException(exception, ::getAllChallenges, retries, error)
          },
        )
      } catch (e: TwilioVerifyException) {
        error(e)
      } catch (e: Exception) {
        Logger.log(Level.Error, e.toString(), e)
        error(TwilioVerifyException(NetworkException(e), NetworkError))
      }
    }
    getAllChallenges()
  }

  private fun updateChallengeURL(challenge: FactorChallenge) =
    challenge.factor?.let { factor ->
      "$baseUrl$UPDATE_CHALLENGE_URL"
        .replace(SERVICE_SID_PATH, factor.serviceSid, true)
        .replace(IDENTITY_PATH, factor.identity)
        .replace(CHALLENGE_SID_PATH, challenge.sid)
    } ?: run {
      throw IllegalArgumentException("ServiceSid or Identity is null or empty")
    }

  private fun updateChallengeBody(authPayload: String): Map<String, String?> =
    mapOf(
      AUTH_PAYLOAD_PARAM to authPayload,
    )

  private fun getChallengeURL(
    challengeSid: String,
    factor: Factor,
  ) = "$baseUrl$GET_CHALLENGE_URL"
    .replace(SERVICE_SID_PATH, factor.serviceSid, true)
    .replace(IDENTITY_PATH, factor.identity)
    .replace(CHALLENGE_SID_PATH, challengeSid)

  private fun getChallengesURL(factor: Factor) =
    "$baseUrl$GET_CHALLENGES_URL"
      .replace(SERVICE_SID_PATH, factor.serviceSid, true)
      .replace(IDENTITY_PATH, factor.identity)
}
