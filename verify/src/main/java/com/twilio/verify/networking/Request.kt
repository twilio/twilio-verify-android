package com.twilio.verify.networking

class Request private constructor(
    httpMethod: HttpMethod,
    url: String,
    body: Map<String, Any>,
    contentType: MediaType,
    acceptType: MediaType,
    headers: Map<String, String>,
    tag: String
) {
    data class Builder(
        val requestHelper: RequestHelper,
        var httpMethod: HttpMethod? = null,
        var url: String? = null,
        var body: Map<String, Any>? = null,
        var contentType: MediaType? = null,
        var acceptType: MediaType? = null,
        var headers: Map<String, String>? = null,
        var tag: String? = null
    ) {
        fun httpMethod(httpMethod: HttpMethod) = apply { this.httpMethod = httpMethod }
        fun url(url: String) = apply { this.url = url }
        fun body(body: Map<String, Any>) = apply { this.body = body }
        fun contentType(contentType: MediaType) = apply { this.contentType = contentType }
        fun acceptType(acceptType: MediaType) = apply { this.acceptType = acceptType }
        fun headers(headers: MutableMap<String, String>) = apply {
            headers.putAll(requestHelper.commonHeaders)
            this.headers = headers
        }
        fun tag(tag: String) = apply { this.tag = tag }

        fun build() = Request(
            httpMethod ?: HttpMethod.Get,
            url ?: "",
            body ?: emptyMap(),
            contentType ?: MediaType.UrlEncoded,
            acceptType ?: MediaType.UrlEncoded,
            headers ?: emptyMap(),
            tag ?: ""
        )
    }
}