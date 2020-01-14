package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
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
    val tag = "tag"
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val request =
      Request.Builder(requestHelper, url)
          .httpMethod(httpMethod)
          .tag(tag)
          .build()

    assertEquals(httpMethod, request.httpMethod)
    assertEquals(url, request.url)
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
    val headers = mutableMapOf("Content-Type" to contentType.type, "Accept-Type" to acceptType.type)
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val request =
      Request.Builder(requestHelper, url)
          .httpMethod(httpMethod)
          .headers(headers)
          .tag(tag)
          .build()

    assertEquals(httpMethod, request.httpMethod)
    assertEquals(url, request.url)
    assertEquals(tag, request.tag)
    assertEquals(headers + requestHelper.commonHeaders, request.headers)
  }
}