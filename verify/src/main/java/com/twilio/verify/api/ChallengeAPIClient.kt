package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.BuildConfig
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
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
  "${BuildConfig.BASE_URL}Services/$serviceSidPath/Entities/$entitySidPath/Factors/$factorSidPath/Challenges/$challengeSidPath"
internal const val getChallengeURL =
  "${BuildConfig.BASE_URL}Services/$serviceSidPath/Entities/$entitySidPath/Factors/$factorSidPath/Challenges/$challengeSidPath"

internal class ChallengeAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization
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
    pushFactor: PushFactor,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(
          requestHelper,
          getChallengeURL(sid, pushFactor)
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
      updateChallengeURL.replace(serviceSidPath, factor.serviceSid, true)
          .replace(
              entitySidPath, factor.entitySid, true
          )
          .replace(challengeSidPath, challenge.sid)
    } ?: run {
      throw IllegalArgumentException("ServiceSid or EntitySid is null or empty")
    }

  private fun updateChallengeBody(
    authPayload: String
  ): Map<String, String?> =
    mapOf(
        authPayloadParam to authPayload
    )

  private fun getChallengeURL(
    challengeSid: String,
    factor: PushFactor
  ) = getChallengeURL.replace(serviceSidPath, factor.serviceSid, true)
      .replace(
          entitySidPath, factor.entitySid, true
      )
      .replace(challengeSidPath, challengeSid)

}