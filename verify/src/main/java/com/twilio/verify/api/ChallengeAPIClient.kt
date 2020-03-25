package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */
internal const val challengeSidPath = "{ChallengeSid}"
internal const val updateChallengeURL =
  "Services/$SERVICE_SID_PATH/Entities/$ENTITY_PATH/Factors/$FACTOR_SID_PATH/Challenges/$challengeSidPath"
internal const val getChallengeURL =
  "Services/$SERVICE_SID_PATH/Entities/$ENTITY_PATH/Factors/$FACTOR_SID_PATH/Challenges/$challengeSidPath"

internal class ChallengeAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization,
  private val baseUrl: String
) {

  fun update(
    challenge: FactorChallenge,
    authPayload: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(
          requestHelper,
          updateChallengeURL(challenge)
      )
          .httpMethod(Post)
          .body(updateChallengeBody(authPayload))
          .build()
      networkProvider.execute(request, {
        success()
      }, { exception ->
        error(TwilioVerifyException(exception, NetworkError))
      })
    } catch (e: Exception) {
      error(TwilioVerifyException(NetworkException(e), NetworkError))
    }
  }

  fun get(
    sid: String,
    factor: Factor,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(
          requestHelper,
          getChallengeURL(sid, factor)
      )
          .httpMethod(Get)
          .build()
      networkProvider.execute(request, {
        success(JSONObject(it))
      }, { exception ->
        error(TwilioVerifyException(exception, NetworkError))
      })
    } catch (e: Exception) {
      error(TwilioVerifyException(NetworkException(e), NetworkError))
    }
  }

  private fun updateChallengeURL(challenge: FactorChallenge) =
    challenge.factor?.let { factor ->
      "$baseUrl$updateChallengeURL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
          .replace(
              ENTITY_PATH, factor.entityIdentity, true
          )
          .replace(FACTOR_SID_PATH, factor.sid)
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
      .replace(
          ENTITY_PATH, factor.entityIdentity, true
      )
      .replace(FACTOR_SID_PATH, factor.sid)
      .replace(challengeSidPath, challengeSid)

}