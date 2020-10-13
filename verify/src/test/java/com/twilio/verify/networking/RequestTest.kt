package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
import androidx.test.core.app.ApplicationProvider
import java.net.MalformedURLException
import java.net.URL
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestTest {

  @Test
  fun `Request should match the params that were send to the RequestBuilder, without custom headers`() {
    val httpMethod = HttpMethod.POST
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = BasicAuthorization("accountSid", "authToken")
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
    val httpMethod = HttpMethod.POST
    val url = "https://twilio.com"
    val contentType = MediaTypeValue.URL_ENCODED
    val acceptType = MediaTypeValue.URL_ENCODED
    val tag = "tag"
    val headers = mutableMapOf("Content-Type" to contentType.type, "Accept-Type" to acceptType.type)
    val authorization = BasicAuthorization("accountSid", "authToken")
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
    val httpMethod = HttpMethod.POST
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = BasicAuthorization("accountSid", "authToken")
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
            MediaTypeHeader.CONTENT_TYPE.type to MediaTypeValue.URL_ENCODED.type
          ).toMutableMap()
        )
        .build()
    assertEquals(expectedParams, request.getParams())
  }

  @Test
  fun `Request params should be jsonParams`() {
    val httpMethod = HttpMethod.POST
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = BasicAuthorization("accountSid", "authToken")
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
          mapOf(MediaTypeHeader.CONTENT_TYPE.type to MediaTypeValue.JSON.type).toMutableMap()
        )
        .build()

    assertEquals(expectedParams.toString(), request.getParams())
  }

  @Test
  fun `Request params without body should be empty`() {
    val httpMethod = HttpMethod.POST
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = BasicAuthorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val expectedParams = ""
    val request =
      Request.Builder(requestHelper, url)
        .httpMethod(httpMethod)
        .tag(tag)
        .headers(
          mapOf(MediaTypeHeader.CONTENT_TYPE.type to MediaTypeValue.JSON.type).toMutableMap()
        )
        .build()

    assertEquals(expectedParams, request.getParams())
  }

  @Test(expected = MalformedURLException::class)
  fun `Trying to create a Request with invalid URL should throw MalformedURLException`() {
    val httpMethod = HttpMethod.POST
    val url = ""
    val tag = "tag"
    val authorization = BasicAuthorization("accountSid", "authToken")
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

  @Test
  fun `Request queryparams should be appended to URL`() {
    val httpMethod = HttpMethod.POST
    val url = "https://twilio.com"
    val tag = "tag"
    val authorization = BasicAuthorization("accountSid", "authToken")
    val requestHelper =
      RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val key1 = "Key"
    val value1 = "Value"
    val key2 = "Twilio"
    val value2 = "Authy"
    val expectedURl = "$url?$key1=$value1&$key2=$value2"
    val request =
      Request.Builder(requestHelper, url)
        .httpMethod(httpMethod)
        .tag(tag)
        .query(mapOf(key1 to value1, key2 to value2))
        .build()

    assertEquals(expectedURl, request.url.toString())
  }
}
