package com.twilio.verify.sample.networking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.networking.HttpMethod.Post
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class OkHttpProviderTest {
  private val okHttpClient: OkHttpClient = mock()
  private val okHttpProvider = OkHttpProvider(okHttpClient)

  @Test
  fun `Execute request with success response should call success`() {
    val request: Request = mock() {
      on { url } doReturn URL("https://twilio.com")
      on { httpMethod } doReturn Post
    }
    val call: Call = mock()
    whenever(okHttpClient.newCall(any())).thenReturn(call)
    val bodyJson = JSONObject()
    val responseBody: ResponseBody = mock {
      on { string() } doReturn bodyJson.toString()
    }
    val expectedHeaders = mapOf("header" to listOf("value"))
    val headersBuilder = Headers.Builder()
    expectedHeaders.forEach { header ->
      header.value.forEach { value ->
        headersBuilder.add(header.key, value)
      }
    }
    val response = Response.Builder()
        .body(responseBody)
        .headers(headersBuilder.build())
        .message("message")
        .code(200)
        .protocol(mock())
        .request(mock())
        .build()
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onResponse(call, response)
      }
    }
    okHttpProvider.execute(request, {
      assertEquals(bodyJson.toString(), it.body)
      assertEquals(expectedHeaders, it.headers)
    }, {
      fail()
    })
  }

  @Test
  fun `Execute request with success response but with invalid response code should call error`() {
    val request: Request = mock() {
      on { url } doReturn URL("https://twilio.com")
      on { httpMethod } doReturn Post
    }
    val call: Call = mock()
    whenever(okHttpClient.newCall(any())).thenReturn(call)
    val bodyJson = JSONObject()
    val responseBody: ResponseBody = mock() {
      on { string() } doReturn bodyJson.toString()
    }
    val response = Response.Builder()
        .body(responseBody)
        .message("message")
        .code(400)
        .protocol(mock())
        .request(mock())
        .build()
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onResponse(call, response)
      }
    }
    okHttpProvider.execute(request, {
      fail()
    }, {
      assertEquals(NetworkException::class, it::class)
    })
  }

  @Test
  fun `Execute request with failure response should call error`() {
    val request: Request = mock() {
      on { url } doReturn URL("https://twilio.com")
      on { httpMethod } doReturn Post
    }
    val call: Call = mock()
    whenever(okHttpClient.newCall(any())).thenReturn(call)
    val expectedException: IOException = mock()
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onFailure(call, expectedException)
      }
    }
    okHttpProvider.execute(request, {
      fail()
    }, {
      assertEquals(expectedException, it.cause)
    })
  }
}