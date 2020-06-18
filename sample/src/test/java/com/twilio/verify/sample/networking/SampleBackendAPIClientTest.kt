package com.twilio.verify.sample.networking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.sample.IdlingResource
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Ignore
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
  private val sampleBackendAPIClient = backendAPIClient(okHttpClient)
  private val idlingResource = IdlingResource()

  @Ignore
  @Test
  fun `Enrollment with success response should return enrollment response`() {
    val identity = "identity"
    val call: Call = mock()
    val tokenValue = "jweToken"
    val serviceSidValue = "serviceSidValue"
    val factorTypeValue = "push"
    val identityValue = "identityValue"
    val bodyJson = JSONObject().apply {
      put("token", tokenValue)
      put("serviceSid", serviceSidValue)
      put("factorType", factorTypeValue)
      put("identity", identityValue)
    }
    val responseBody: ResponseBody = mock {
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
    idlingResource.startOperation()
    sampleBackendAPIClient.getEnrollmentResponse(identity, { enrollmentResponse ->
      assertEquals(tokenValue, enrollmentResponse.token)
      assertEquals(serviceSidValue, enrollmentResponse.serviceSid)
      assertEquals(factorTypeValue, enrollmentResponse.factorType.factorTypeName)
      assertEquals(identityValue, enrollmentResponse.identity)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Ignore
  @Test(expected = IllegalArgumentException::class)
  fun `Enrollment with success response but with invalid factorType should throw exception`() {
    runBlocking {
      val identity = "identity"
      val call: Call = mock()
      val tokenValue = "jweToken"
      val serviceSidValue = "serviceSidValue"
      val factorTypeValue = "whatever"
      val identityValue = "identityValue"
      val bodyJson = JSONObject().apply {
        put("token", tokenValue)
        put("serviceSid", serviceSidValue)
        put("factorType", factorTypeValue)
        put("identity", identityValue)
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
      sampleBackendAPIClient.enrollment(identity)
          ?.execute()
    }
  }

  @Ignore
  @Test(expected = Exception::class)
  fun `Enrollment with fail response should throw exception`() {
    val identity = "identity"
    val call: Call = mock()
    val expectedException: IOException = mock()
    whenever(okHttpClient.newCall(any())).thenReturn(call)
    argumentCaptor<(Callback)>().apply {
      whenever(call.enqueue(capture())).then {
        lastValue.onFailure(call, expectedException)
      }
    }
    sampleBackendAPIClient.enrollment(identity)
        ?.execute()
  }

  @Ignore
  @Test(expected = IOException::class)
  fun `Enrollment with success response but with invalid response code should return throw exception`() {
    val identity = "identity"
    val call: Call = mock()
    val tokenValue = "jweToken"
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
    sampleBackendAPIClient.enrollment(identity)
        ?.execute()
  }
}