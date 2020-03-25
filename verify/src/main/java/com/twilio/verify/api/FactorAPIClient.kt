package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.BuildConfig
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.BasicAuthorization
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
internal const val serviceSidPath = "{ServiceSid}"
internal const val entityPath = "{EntityIdentity}"
internal const val factorSidPath = "{FactorSid}"

internal const val authPayloadParam = "AuthPayload"

internal const val createFactorURL =
  "Services/$serviceSidPath/Entities/$entityPath/Factors"
internal const val verifyFactorURL =
  "Services/$serviceSidPath/Entities/$entityPath/Factors/$factorSidPath"
internal const val updateFactorURL =
  "Services/$serviceSidPath/Entities/$entityPath/Factors/$factorSidPath"

internal const val fcmPushType = "fcm"
internal const val friendlyName = "FriendlyName"
internal const val factorType = "FactorType"
internal const val binding = "Binding"
internal const val config = "Config"
internal const val sdkVersionKey = "sdk_version"
internal const val appIdKey = "app_id"
internal const val notificationPlatformKey = "notification_platform"
internal const val notificationTokenKey = "notification_token"
internal const val algKey = "alg"
internal const val defaultAlg = "ES256"
internal const val jwtAuthenticationUser = "token"
internal const val publicKeyKey = "public_key"

internal class FactorAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization,
  private val baseUrl: String
) {

  fun create(
    createFactorPayload: CreateFactorPayload,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper =
        RequestHelper(context, BasicAuthorization(jwtAuthenticationUser, createFactorPayload.jwt))
      val request = Request.Builder(
          requestHelper,
          createFactorURL(createFactorPayload)
      )
          .httpMethod(Post)
          .body(createFactorBody(createFactorPayload))
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

  fun verify(
    factor: Factor,
    authPayload: String,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(requestHelper, verifyFactorURL(factor))
          .httpMethod(Post)
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

  fun update(
    updateFactorPayload: UpdateFactorPayload,
    success: (response: JSONObject) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(requestHelper, updateFactorURL(updateFactorPayload))
          .httpMethod(Post)
          .body(updateFactorBody(updateFactorPayload))
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

  private fun createFactorURL(createFactorPayload: CreateFactorPayload): String =
    "$baseUrl$createFactorURL".replace(serviceSidPath, createFactorPayload.serviceSid, true)
        .replace(
            entityPath, createFactorPayload.entity, true
        )

  private fun verifyFactorURL(factor: Factor): String =
    "$baseUrl$verifyFactorURL".replace(serviceSidPath, factor.serviceSid, true)
        .replace(
            entityPath, factor.entityIdentity, true
        ).replace(factorSidPath, factor.sid)

  private fun updateFactorURL(
    updateFactorPayload: UpdateFactorPayload
  ): String =
    "$baseUrl$updateFactorURL".replace(serviceSidPath, updateFactorPayload.serviceSid, true)
        .replace(
            entityPath, updateFactorPayload.entity, true
        ).replace(
            factorSidPath, updateFactorPayload.factorSid
        )

  private fun createFactorBody(
    createFactorPayload: CreateFactorPayload
  ): Map<String, String?> =
    mapOf(
        friendlyName to createFactorPayload.friendlyName,
        factorType to createFactorPayload.type.factorTypeName,
        binding to binding(createFactorPayload),
        config to config(createFactorPayload)
    )

  private fun binding(
    createFactorPayload: CreateFactorPayload
  ): String = JSONObject().apply {
    put(publicKeyKey, createFactorPayload.publicKey)
    put(algKey, defaultAlg)
  }.toString()

  private fun config(
    factoryPayload: FactorPayload
  ): String = JSONObject().apply {
    put(sdkVersionKey, BuildConfig.VERSION_NAME)
    put(appIdKey, context.applicationInfo.packageName)
    put(notificationPlatformKey, fcmPushType)
    put(notificationTokenKey, factoryPayload.pushToken)
  }.toString()

  private fun verifyFactorBody(authPayload: String): Map<String, String?> =
    mapOf(authPayloadParam to authPayload)

  private fun updateFactorBody(
    updateFactorPayload: UpdateFactorPayload
  ): Map<String, String?> =
    mapOf(
        friendlyName to updateFactorPayload.friendlyName,
        config to config(updateFactorPayload)
    )
}