package com.twilio.verify.sample.networking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.sample.networking.SampleBackendAPIClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class SampleBackendAPIClientTest {
  private val okHttpClient: OkHttpClient = mock()
  private val sampleBackendAPIClient = SampleBackendAPIClient(okHttpClient)

  @Test
  fun `Get JWT with success response should return JWT token`() {
    runBlocking {
      val url = "https://twilio.com"
      val identity = "identity"
      val call: Call = mock()
      val tokenValue = "jwtToken"
      val bodyJson = JSONObject().apply {
        put("token", tokenValue)
      }
      val responseBody: ResponseBody = mock() {
        on { string() } doReturn bodyJson.toString()
      }
      val response = Response.Builder()
          .body(responseBody)
          .message("message")
          .code(200)
          .protocol(mock())
          .request(mock())
          .build()
      whenever(okHttpClient.newCall(any())).thenReturn(call)
      argumentCaptor<(Callback)>().apply {
        whenever(call.enqueue(capture())).then {
          lastValue.onResponse(call, response)
        }
      }
      val token = sampleBackendAPIClient.getJwt(url, identity, Dispatchers.Unconfined)
      assertEquals(tokenValue, token)
    }
  }

  @Test(expected = Exception::class)
  fun `Get JWT with fail response should throw exception`() {
    runBlocking {
      val url = "https://twilio.com"
      val identity = "identity"
      val call: Call = mock()
      val expectedException: IOException = mock()
      whenever(okHttpClient.newCall(any())).thenReturn(call)
      argumentCaptor<(Callback)>().apply {
        whenever(call.enqueue(capture())).then {
          lastValue.onFailure(call, expectedException)
        }
      }
      sampleBackendAPIClient.getJwt(url, identity, Dispatchers.Unconfined)
    }
  }

  @Test(expected = IOException::class)
  fun `Get JWT with success response but with invalid response code should return throw exception`() {
    runBlocking {
      val url = "https://twilio.com"
      val identity = "identity"
      val call: Call = mock()
      val tokenValue = "jwtToken"
      val bodyJson = JSONObject().apply {
        put("token", tokenValue)
      }
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
      whenever(okHttpClient.newCall(any())).thenReturn(call)
      argumentCaptor<(Callback)>().apply {
        whenever(call.enqueue(capture())).then {
          lastValue.onResponse(call, response)
        }
      }
      sampleBackendAPIClient.getJwt(url, identity, Dispatchers.Unconfined)
    }
  }
}