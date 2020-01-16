package com.twilio.verify.networking

import android.net.Uri
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */
class Request internal constructor(
  internal val httpMethod: HttpMethod,
  internal val url: URL,
  internal val body: Map<String, Any?>?,
  internal val headers: Map<String, String>,
  internal val tag: String
) {

  fun getParams(): String? {
    return when {
      body == null -> ""
      headers[MediaType.ContentType.type] == MediaType.UrlEncoded.type -> queryParams(body)
      headers[MediaType.ContentType.type] == MediaType.Json.type -> jsonParams(body)
      else -> ""
    }
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

    @Throws(MalformedURLException::class)
    fun build() = Request(
        httpMethod ?: HttpMethod.Get,
        URL(url),
        body,
        headers ?: requestHelper.commonHeaders,
        tag ?: ""
    )
  }
}