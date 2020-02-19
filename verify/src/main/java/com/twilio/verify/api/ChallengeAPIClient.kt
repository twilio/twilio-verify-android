package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.BuildConfig
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper

/*
 * Copyright (c) 2020, Twilio Inc.
 */
internal const val challengeSidPath = "{ChallengeSid}"
internal const val updateChallengeURL =
  "${BuildConfig.BASE_URL}Services/$serviceSidPath/Entities/$entityIdPath/Factors/$factorSidPath/Challenges/$challengeSidPath"

internal class ChallengeAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization
): APIClient() {

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
          .headers(postMediaTypeHeaders().toMutableMap())
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

  private fun updateChallengeURL(challenge: FactorChallenge) =
    challenge.factor?.let { factor ->
      updateChallengeURL.replace(serviceSidPath, factor.serviceSid, true)
          .replace(
              entityIdPath, factor.entityId, true
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
}