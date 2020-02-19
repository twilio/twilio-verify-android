package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.MalformedURLException
import java.net.URL

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
    assertEquals(URL(url), request.url)
    assertEquals(tag, request.tag)
    assertEquals(requestHelper.commonHeaders(request.httpMethod), request.headers)
  }

  @Test
  fun `Request should match the params that were send to the RequestBuilder, with custom headers`() {
    val httpMethod = HttpMethod.Post
    val url = "https://twilio.com"
    val contentType = MediaTypeValue.UrlEncoded
    val acceptType = MediaTypeValue.UrlEncoded
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
    assertEquals(URL(url), request.url)
    assertEquals(tag, request.tag)
    assertEquals(headers + requestHelper.commonHeaders(request.httpMethod), request.headers)
  }

  @Test
  fun `Request params should be queryParams`() {
    val httpMethod = HttpMethod.Post
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val key1 = "Key"
    val value1 = "Value"
    val key2 = "Twilio"
    val value2 = "Authy"
    val expectedParams = "$key1=$value1&$key2=$value2"
    val request =
      Request.Builder(requestHelper, url)
          .httpMethod(httpMethod)
          .tag(tag)
          .body(mapOf(key1 to value1, key2 to value2))
          .headers(
              mapOf(
                  MediaTypeHeader.ContentType.type to MediaTypeValue.UrlEncoded.type
              ).toMutableMap()
          )
          .build()

    assertEquals(expectedParams, request.getParams())
  }

  @Test
  fun `Request params should be jsonParams`() {
    val httpMethod = HttpMethod.Post
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val key1 = "Key"
    val value1 = "Value"
    val key2 = "Twilio"
    val value2 = "Authy"
    val expectedParams = JSONObject().apply {
      put(key1, value1)
      put(key2, value2)
    }
    val request =
      Request.Builder(requestHelper, url)
          .httpMethod(httpMethod)
          .tag(tag)
          .body(mapOf(key1 to value1, key2 to value2))
          .headers(
              mapOf(MediaTypeHeader.ContentType.type to MediaTypeValue.Json.type).toMutableMap()
          )
          .build()

    assertEquals(expectedParams.toString(), request.getParams())
  }

  @Test
  fun `Request params without body should be empty`() {
    val httpMethod = HttpMethod.Post
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val expectedParams = ""
    val request =
      Request.Builder(requestHelper, url)
          .httpMethod(httpMethod)
          .tag(tag)
          .headers(
              mapOf(MediaTypeHeader.ContentType.type to MediaTypeValue.Json.type).toMutableMap()
          )
          .build()

    assertEquals(expectedParams, request.getParams())
  }

  @Test(expected = MalformedURLException::class)
  fun `Trying to create a Request with invalid URL should throw MalformedURLException`() {
    val httpMethod = HttpMethod.Post
    val url = ""
    val tag = "tag"
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val key1 = "Key"
    val value1 = "Value"
    val key2 = "Twilio"
    val value2 = "Authy"
    Request.Builder(requestHelper, url)
        .httpMethod(httpMethod)
        .tag(tag)
        .body(mapOf(key1 to value1, key2 to value2))
        .build()

    fail()
  }
}