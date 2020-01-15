package com.twilio.verify.networking

import android.net.Uri
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */
class Request private constructor(
  internal val httpMethod: HttpMethod,
  internal val url: String,
  internal val body: Map<String, Any?>?,
  internal val headers: Map<String, String>,
  internal val tag: String
) {

  fun getParams(): String? {
    if (body != null) {
      return if (headers[MediaType.ContentType.type] == MediaType.UrlEncoded.type) {
        queryParams(body)
      } else {
        jsonParams(body)
      }
    }
    return ""
  }

  private fun queryParams(params: Map<String, Any?>): String? {
    val builder = Uri.Builder()
    for ((key, value) in params) {
      builder.appendQueryParameter(key, value.toString())

    }
    return builder.build()
        .encodedQuery
  }

  private fun jsonParams(params: Map<String, Any?>): String? {
    val jsonObject = JSONObject()
    for ((key, value) in params) {
      jsonObject.put(key, value)
    }
    return jsonObject.toString()
  }

  data class Builder(
    private val requestHelper: RequestHelper,
    private val url: String,
    private var httpMethod: HttpMethod? = HttpMethod.Get,
    private var body: Map<String, Any?>? = null,
    private var headers: Map<String, String>? = null,
    private var tag: String? = null
  ) {
    fun httpMethod(httpMethod: HttpMethod) = apply { this.httpMethod = httpMethod }
    fun body(body: Map<String, Any?>) = apply { this.body = body }
    fun headers(headers: MutableMap<String, String>) = apply {
      this.headers = headers + requestHelper.commonHeaders
    }

    fun tag(tag: String) = apply { this.tag = tag }

    fun build() = Request(
        httpMethod ?: HttpMethod.Get,
        url,
        body,
        headers ?: requestHelper.commonHeaders,
        tag ?: ""
    )
  }
}