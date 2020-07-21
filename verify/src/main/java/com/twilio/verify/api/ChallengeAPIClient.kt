package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateAdapter
import com.twilio.verify.data.DateProvider
import com.twilio.verify.domain.challenge.models.FactorChallenge
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

/*
 * Copyright (c) 2020, Twilio Inc.
 */
internal const val challengeSidPath = "{ChallengeSid}"
internal const val statusParameter = "Status"
internal const val pageSizeParameter = "PageSize"
internal const val pageTokenParameter = "PageToken"
internal const val signatureFieldsHeader = "Twilio-Verify-Signature-Fields"
internal const val updateChallengeURL =
  "Services/$SERVICE_SID_PATH/Entities/$IDENTITY_PATH/Challenges/$challengeSidPath"
internal const val getChallengeURL =
  "Services/$SERVICE_SID_PATH/Entities/$IDENTITY_PATH/Challenges/$challengeSidPath"
internal const val getChallengesURL =
  "Services/$SERVICE_SID_PATH/Entities/$IDENTITY_PATH/Challenges"

internal const val FACTOR_SID_KEY = "FactorSid"

internal class ChallengeAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authentication: Authentication,
  private val baseUrl: String,
  dateProvider: DateProvider = DateAdapter(
      storagePreferences(context)
  )
) : BaseAPIClient(dateProvider) {

  fun update(
    challenge: FactorChallenge,
    authPayload: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val factor = challenge.factor ?: throw IllegalArgumentException(
          "Factor is null"
      )
      val authToken = authentication.generateJWT(factor)
      val requestHelper = RequestHelper(
          context,
          BasicAuthorization(AUTHENTICATION_USER, authToken)
      )
      val request = Request.Builder(
          requestHelper,
          updateChallengeURL(challenge)
      )
          .httpMethod(Post)
          .body(updateChallengeBody(authPayload))
          .build()
      networkProvider.execute(request, {
        success()
      }, { date ->
        syncTime(date)
        update(challenge, authPayload, success, error)
      }, { exception ->
        error(TwilioVerifyException(exception, NetworkError))
      })
    } catch (e: TwilioVerifyException) {
      error(e)
    } catch (e: Exception) {
      error(TwilioVerifyException(NetworkException(e), NetworkError))
    }
  }

  fun get(
    sid: String,
    factor: Factor,
    success: (response: JSONObject, signatureFieldsHeader: String?) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val authToken = authentication.generateJWT(factor)
      val requestHelper =
        RequestHelper(context, BasicAuthorization(AUTHENTICATION_USER, authToken))
      val request = Request.Builder(
          requestHelper,
          getChallengeURL(sid, factor)
      )
          .httpMethod(Get)
          .build()
      networkProvider.execute(request, {
        success(
            JSONObject(it.body),
            it.headers[signatureFieldsHeader]?.first()
        )
      }, { date ->
        syncTime(date)
        get(sid, factor, success, error)
      }, { exception ->
        error(TwilioVerifyException(exception, NetworkError))
      })
    } catch (e: TwilioVerifyException) {
      error(e)
    } catch (e: Exception) {
      error(TwilioVerifyException(NetworkException(e), NetworkError))
    }
  }

  fun getAll(
    factor: Factor,
    status: String?,
    pageSize: Int,
    pageToken: String?,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val authToken = authentication.generateJWT(factor)
      val requestHelper =
        RequestHelper(context, BasicAuthorization(AUTHENTICATION_USER, authToken))
      val queryParameters = mutableMapOf<String, Any>(
          pageSizeParameter to pageSize,
          FACTOR_SID_KEY to factor.sid
      )
      status?.let {
        queryParameters.put(statusParameter, it)
      }
      pageToken?.let {
        queryParameters.put(pageTokenParameter, it)
      }
      val request = Request.Builder(
          requestHelper,
          getChallengesURL(factor)
      )
          .httpMethod(Get)
          .query(queryParameters)
          .build()
      networkProvider.execute(request, {
        success(JSONObject(it.body))
      }, { date ->
        syncTime(date)
        getAll(factor, status, pageSize, pageToken, success, error)
      }, { exception ->
        error(TwilioVerifyException(exception, NetworkError))
      })

    } catch (e: TwilioVerifyException) {
      error(e)
    } catch (e: Exception) {
      error(TwilioVerifyException(NetworkException(e), NetworkError))
    }
  }

  private fun updateChallengeURL(challenge: FactorChallenge) =
    challenge.factor?.let { factor ->
      "$baseUrl$updateChallengeURL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
          .replace(IDENTITY_PATH, factor.entityIdentity)
          .replace(challengeSidPath, challenge.sid)
    } ?: run {
      throw IllegalArgumentException("ServiceSid or EntityIdentity is null or empty")
    }

  private fun updateChallengeBody(
    authPayload: String
  ): Map<String, String?> =
    mapOf(
        AUTH_PAYLOAD_PARAM to authPayload
    )

  private fun getChallengeURL(
    challengeSid: String,
    factor: Factor
  ) = "$baseUrl$getChallengeURL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
      .replace(IDENTITY_PATH, factor.entityIdentity)
      .replace(challengeSidPath, challengeSid)

  private fun getChallengesURL(
    factor: Factor
  ) = "$baseUrl$getChallengesURL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
      .replace(IDENTITY_PATH, factor.entityIdentity)
}

