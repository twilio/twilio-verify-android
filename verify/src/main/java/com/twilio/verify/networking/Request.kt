package com.twilio.verify.networking

class Request private constructor(
  internal val httpMethod: HttpMethod,
  internal val url: String,
  internal val body: Map<String, Any>?,
  internal val headers: Map<String, String>,
  internal val tag: String
) {
  data class Builder(
    private val requestHelper: RequestHelper,
    private val url: String,
    private var httpMethod: HttpMethod? = HttpMethod.Get,
    private var body: Map<String, Any>? = null,
    private var contentType: MediaType? = null,
    private var acceptType: MediaType? = null,
    private var headers: Map<String, String>? = null,
    private var tag: String? = null
  ) {
    fun httpMethod(httpMethod: HttpMethod) = apply { this.httpMethod = httpMethod }
    fun body(body: Map<String, Any>) = apply { this.body = body }
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