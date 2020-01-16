package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.domain.factor.models.FactorBuilder
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.HttpMethod
import com.twilio.verify.networking.MediaType
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.RequestHelper
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val serviceSidPath = "{ServiceSid}"
internal const val userIdPath = "{UserId}"
internal const val url =
  "https://authy.twilio.com/v1/Services/$serviceSidPath/Entities/$userIdPath/Factors"
internal const val friendlyName = "FriendlyName"
internal const val factorType = "FactorType"
internal const val binding = "Binding"

class FactorAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization
) {

  internal fun create(
    factorBuilder: FactorBuilder,
    success: (response: JSONObject) -> Unit,
    error: () -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(
          requestHelper,
          url(factorBuilder)
      )
          .httpMethod(HttpMethod.Post)
          .headers(headers().toMutableMap())
          .body(body(factorBuilder))
          .build()
      networkProvider.execute(request, {
        success(JSONObject(it))
      }, {
        error()
      })
    } catch (e: Exception) {
      error()
    }

  }

  private fun url(factorBuilder: FactorBuilder): String {
    return url.replace(serviceSidPath, factorBuilder.serviceSid ?: "", true)
        .replace(
            userIdPath, factorBuilder.userId ?: "", true
        )
  }

  private fun headers(): Map<String, String> =
    mapOf(
        MediaType.Accept.type to MediaType.Json.type,
        MediaType.ContentType.type to MediaType.UrlEncoded.type
    )

  private fun body(factorBuilder: FactorBuilder): Map<String, String?> =
    mapOf(
        friendlyName to factorBuilder.friendlyName,
        factorType to factorBuilder.type?.factorTypeName,
        binding to factorBuilder.binding.values.joinToString("|")
    )
}