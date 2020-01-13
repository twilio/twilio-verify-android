package com.twilio.verify.networking

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestTest {

  @Test
  fun `Request should match the params that were send to the RequestBuilder, without custom headers`() {
    val httpMethod = HttpMethod.Post
    val url = "https://twilio.com"
    val contentType = MediaType.UrlEncoded
    val acceptType = MediaType.UrlEncoded
    val tag = "tag"
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val request =
      Request.Builder(requestHelper)
          .httpMethod(httpMethod)
          .url(url)
          .contentType(contentType)
          .acceptType(acceptType)
          .tag(tag)
          .build()

    assertEquals(httpMethod, request.httpMethod)
    assertEquals(url, request.url)
    assertEquals(contentType, request.contentType)
    assertEquals(acceptType, request.acceptType)
    assertEquals(tag, request.tag)
    assertEquals(requestHelper.commonHeaders, request.headers)
  }

  @Test
  fun `Request should match the params that were send to the RequestBuilder, with custom headers`() {
    val httpMethod = HttpMethod.Post
    val url = "https://twilio.com"
    val contentType = MediaType.UrlEncoded
    val acceptType = MediaType.UrlEncoded
    val tag = "tag"
    val headers = mutableMapOf("test1" to "test1", "test2" to "test2")
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val request =
      Request.Builder(requestHelper)
          .httpMethod(httpMethod)
          .url(url)
          .contentType(contentType)
          .acceptType(acceptType)
          .headers(headers)
          .tag(tag)
          .build()

    assertEquals(httpMethod, request.httpMethod)
    assertEquals(url, request.url)
    assertEquals(contentType, request.contentType)
    assertEquals(acceptType, request.acceptType)
    assertEquals(tag, request.tag)
    assertEquals(headers + requestHelper.commonHeaders, request.headers)
  }
}