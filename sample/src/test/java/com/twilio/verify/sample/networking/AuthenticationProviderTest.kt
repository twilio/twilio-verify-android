package com.twilio.verify.sample.networking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.sample.networking.AuthenticationProvider
import com.twilio.sample.networking.authenticationEndpoint
import com.twilio.verify.api.Action.CREATE
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class AuthenticationProviderTest {
  private val okHttpClient: OkHttpClient = mock()
  private val url = "https://twilio.com"
  private val authenticationProvider = AuthenticationProvider(url, okHttpClient)

  @Test
  fun `Execute request with success response should call success`() {
    val call: Call = mock()
    val tokenValue = "jwtToken"
    whenever(okHttpClient.newCall(any())).thenReturn(call)
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
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onResponse(call, response)
      }
    }
    authenticationProvider.generateJWE(
        "identity", "factorSid", "challengeSid", "serviceSid", CREATE, {
      assertEquals(tokenValue, it)
      verify(okHttpClient).newCall(check { request ->
        assertEquals("POST", request.method)
        assertEquals("$url$authenticationEndpoint", request.url.toString())
      })
    }, {
      fail()
    })
  }

  @Test
  fun `Execute request with success response but with invalid response body should call error with JSONException`() {
    val call: Call = mock()
    whenever(okHttpClient.newCall(any())).thenReturn(call)
    val bodyJson = JSONObject()
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
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onResponse(call, response)
      }
    }
    authenticationProvider.generateJWE(
        "identity", "factorSid", "challengeSid", "serviceSid", CREATE, {
      fail()
    }, { exception ->
      assertEquals(JSONException::class, exception::class)
    })
  }

  @Test
  fun `Execute request with failure response should call error`() {
    val call: Call = mock()
    whenever(okHttpClient.newCall(any())).thenReturn(call)
    val expectedException: IOException = mock()
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onFailure(call, expectedException)
      }
    }
    authenticationProvider.generateJWE(
        "identity", "factorSid", "challengeSid", "serviceSid", CREATE, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception)
    })
  }
}