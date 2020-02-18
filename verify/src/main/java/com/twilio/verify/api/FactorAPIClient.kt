package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.BuildConfig
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.publicKeyKey
import com.twilio.verify.domain.factor.pushTokenKey
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.MediaTypeHeader
import com.twilio.verify.networking.MediaTypeValue
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val serviceSidPath = "{ServiceSid}"
internal const val entitySidPath = "{EntitySid}"
internal const val factorSidPath = "{FactorSid}"
internal const val createFactorURL =
  "${BuildConfig.BASE_URL}Services/$serviceSidPath/Entities/$entitySidPath/Factors"
internal const val verifyFactorURL =
  "${BuildConfig.BASE_URL}Services/$serviceSidPath/Entities/$entitySidPath/Factors/$factorSidPath"
internal const val friendlyName = "FriendlyName"
internal const val factorType = "FactorType"
internal const val binding = "Binding"
internal const val authPayloadParam = "AuthPayload"
internal const val typeKey = "type"
internal const val applicationKey = "application"
internal const val fcmPushType = "fcm"

internal class FactorAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization
) {

  fun create(
    factorPayload: FactorPayload,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(
          requestHelper,
          createFactorURL(factorPayload)
      )
          .httpMethod(Post)
          .headers(headers().toMutableMap())
          .body(createFactorBody(factorPayload, context))
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

  internal fun verify(
    factor: Factor,
    authPayload: String,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(requestHelper, verifyFactorURL(factor))
          .httpMethod(Post)
          .headers(headers().toMutableMap())
          .body(verifyFactorBody(authPayload))
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

  private fun createFactorURL(factorPayload: FactorPayload): String =
    createFactorURL.replace(serviceSidPath, factorPayload.serviceSid, true)
        .replace(
            entitySidPath, factorPayload.entitySid, true
        )

  private fun verifyFactorURL(factor: Factor): String =
    verifyFactorURL.replace(serviceSidPath, factor.serviceSid, true)
        .replace(
            entitySidPath, factor.entitySid, true
        ).replace(factorSidPath, factor.sid)

  private fun headers(): Map<String, String> =
    mapOf(
        MediaTypeHeader.Accept.type to MediaTypeValue.Json.type,
        MediaTypeHeader.ContentType.type to MediaTypeValue.UrlEncoded.type
    )

  private fun createFactorBody(
    factorPayload: FactorPayload,
    context: Context
  ): Map<String, String?> =
    mapOf(
        friendlyName to factorPayload.friendlyName,
        factorType to factorPayload.type.factorTypeName,
        binding to binding(factorPayload, context)
    )

  private fun binding(
    factorPayload: FactorPayload,
    context: Context
  ): String = JSONObject().apply {
    put(pushTokenKey, factorPayload.binding[pushTokenKey])
    put(publicKeyKey, factorPayload.binding[publicKeyKey])
    put(typeKey, fcmPushType)
    put(applicationKey, context.applicationInfo.loadLabel(context.packageManager))
  }.toString()

  private fun verifyFactorBody(authPayload: String): Map<String, String?> =
    mapOf(authPayloadParam to authPayload)
}