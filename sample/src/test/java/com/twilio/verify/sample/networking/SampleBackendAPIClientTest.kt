package com.twilio.verify.sample.networking

import com.twilio.verify.sample.IdlingResource
import java.io.IOException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
@Ignore
class SampleBackendAPIClientTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var sampleBackendAPIClient: SampleBackendAPIClient
  private val idlingResource = IdlingResource()
  private lateinit var url: String

  @Before
  fun before() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    url = mockWebServer.url("/accessTokens")
      .toString()
    sampleBackendAPIClient = backendAPIClient(url)
  }

  @Test
  fun `Access token with success response should return access token response`() {
    val identity = "identity"
    val tokenValue = "accessToken"
    val serviceSidValue = "serviceSidValue"
    val factorTypeValue = "push"
    val identityValue = "identityValue"
    val bodyJson = JSONObject().apply {
      put("token", tokenValue)
      put("serviceSid", serviceSidValue)
      put("factorType", factorTypeValue)
      put("identity", identityValue)
    }
    val mockResponse = MockResponse().setResponseCode(200)
      .setBody(bodyJson.toString())
    mockWebServer.enqueue(mockResponse)
    idlingResource.startOperation()
    sampleBackendAPIClient.getAccessTokenResponse(
      identity, url,
      { accessTokenResponse ->
        assertEquals(tokenValue, accessTokenResponse.token)
        assertEquals(serviceSidValue, accessTokenResponse.serviceSid)
        assertEquals(factorTypeValue, accessTokenResponse.factorType)
        assertEquals(identityValue, accessTokenResponse.identity)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Access token with success response but with invalid response code should return throw exception`() {
    val identity = "identity"
    val bodyJson = JSONObject().apply {
      put("field", "value")
    }
    val mockResponse = MockResponse().setResponseCode(200)
      .setBody(bodyJson.toString())
    mockWebServer.enqueue(mockResponse)
    idlingResource.startOperation()
    sampleBackendAPIClient.getAccessTokenResponse(
      identity, url,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception is IOException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Access token with error response should return throw exception`() {
    val identity = "identity"
    val bodyJson = JSONObject().apply {
      put("error", "25000")
      put("message", "Error")
    }
    val mockResponse = MockResponse().setResponseCode(500)
      .setBody(bodyJson.toString())
    mockWebServer.enqueue(mockResponse)
    idlingResource.startOperation()
    sampleBackendAPIClient.getAccessTokenResponse(
      identity, url,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception is IOException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }
}
