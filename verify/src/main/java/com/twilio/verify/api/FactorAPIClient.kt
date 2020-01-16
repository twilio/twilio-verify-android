package com.twilio.verify.api

import android.content.Context
import com.twilio.verify.BuildConfig
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.networking.*
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val serviceSidPath = "{ServiceSid}"
internal const val userIdPath = "{UserId}"
internal const val url =
  "${BuildConfig.BASE_URL}Services/$serviceSidPath/Entities/$userIdPath/Factors"
internal const val friendlyName = "FriendlyName"
internal const val factorType = "FactorType"
internal const val binding = "Binding"

class FactorAPIClient(
  private val networkProvider: NetworkProvider = NetworkAdapter(),
  private val context: Context,
  private val authorization: Authorization
) {

  internal fun create(
    factorPayload: FactorPayload,
    success: (response: JSONObject) -> Unit,
    error: () -> Unit
  ) {
    try {
      val requestHelper = RequestHelper(context, authorization)
      val request = Request.Builder(
          requestHelper,
          url(factorPayload)
      )
          .httpMethod(HttpMethod.Post)
          .headers(headers().toMutableMap())
          .body(body(factorPayload))
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

  private fun url(factorPayload: FactorPayload): String =
    url.replace(serviceSidPath, factorPayload.serviceSid, true)
        .replace(
            userIdPath, factorPayload.entityId, true
        )

  private fun headers(): Map<String, String> =
    mapOf(
        MediaTypeHeader.Accept.type to MediaTypeValue.Json.type,
        MediaTypeHeader.ContentType.type to MediaTypeValue.UrlEncoded.type
    )

  private fun body(factorPayload: FactorPayload): Map<String, String?> =
    mapOf(
        friendlyName to factorPayload.friendlyName,
        factorType to factorPayload.type.factorTypeName,
        binding to factorPayload.binding.values.joinToString("|")
    )
}