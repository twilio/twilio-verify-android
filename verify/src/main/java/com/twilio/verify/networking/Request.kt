package com.twilio.verify.networking

import android.net.Uri
import java.net.MalformedURLException
import java.net.URL
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */
class Request internal constructor(
  val httpMethod: HttpMethod,
  val url: URL,
  val body: Map<String, Any?>?,
  val headers: Map<String, String>,
  val tag: String
) {

  fun getParams(): String? {
    return when {
      body == null -> ""
      headers[MediaTypeHeader.ContentType.type] == MediaTypeValue.UrlEncoded.type -> queryParams(
        body
      )
      headers[MediaTypeHeader.ContentType.type] == MediaTypeValue.Json.type -> jsonParams(body)
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

  private fun jsonParams(params: Map<String, Any?>): String {
    val jsonObject = JSONObject()
    for ((key, value) in params) {
      jsonObject.put(key, value)
    }
    return jsonObject.toString()
  }

  data class Builder(
    private val requestHelper: RequestHelper,
    private val url: String,
    private var httpMethod: HttpMethod = HttpMethod.Get,
    private var body: Map<String, Any?>? = null,
    private var query: Map<String, Any?>? = null,
    private var headers: Map<String, String>? = null,
    private var tag: String? = null
  ) {
    fun httpMethod(httpMethod: HttpMethod) = apply { this.httpMethod = httpMethod }
    fun body(body: Map<String, Any?>) = apply { this.body = body }
    fun query(query: Map<String, Any?>) = apply { this.query = query }
    fun headers(headers: MutableMap<String, String>) = apply {
      this.headers = headers
    }

    fun tag(tag: String) = apply { this.tag = tag }

    @Throws(MalformedURLException::class)
    fun build() = Request(
      httpMethod,
      URL(addQueryParams()),
      body,
      headers?.let {
        requestHelper.commonHeaders(httpMethod)
          .plus(it)
      } ?: requestHelper.commonHeaders(httpMethod),
      tag ?: ""
    )

    private fun addQueryParams(): String {
      val builder = Uri.parse(url)
        .buildUpon()
      query?.let {
        for ((key, value) in it) {
          builder.appendQueryParameter(key, value.toString())
        }
      }
      return builder.build()
        .toString()
    }
  }
}
