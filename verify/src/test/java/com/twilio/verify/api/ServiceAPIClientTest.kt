package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.Authentication
import com.twilio.verify.BuildConfig
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.Action.FETCH
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.MediaTypeHeader.Accept
import com.twilio.verify.networking.MediaTypeHeader.ContentType
import com.twilio.verify.networking.MediaTypeValue.UrlEncoded
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.userAgent
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ServiceAPIClientTest {

  private lateinit var serviceAPIClient: ServiceAPIClient
  private lateinit var networkProvider: NetworkProvider
  private val authentication: Authentication = mock()
  private lateinit var context: Context
  private val baseUrl = BuildConfig.BASE_URL
  private val idlingResource = IdlingResource()

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    serviceAPIClient =
      ServiceAPIClient(networkProvider, context, authentication, baseUrl)
  }

  @Test
  fun `Get a service with auth token successfully generated and a success response should call success`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { entityIdentity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val response = "{\"sid\":\"serviceSid\",\"friendly_name\":\"friendlyName\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(identity),
          factorSid = eq(factorSid),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(FETCH),
          success = capture(),
          error = any()
        )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, { jsonObject ->
      assertEquals(response, jsonObject.toString())
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a service with auth token generation failed should call error`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { entityIdentity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(identity),
          factorSid = eq(factorSid),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(FETCH),
          success = any(),
          error = capture()
        )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a service with with auth token successfully generated an error response should call error`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { entityIdentity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(identity),
          factorSid = eq(factorSid),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(FETCH),
          success = capture(),
          error = any()
        )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting a service should call error`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { entityIdentity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(identity),
          factorSid = eq(factorSid),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(FETCH),
          success = capture(),
          error = any()
        )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      Assert.assertTrue(exception.cause is NetworkException)
      Assert.assertTrue(exception.cause?.cause is RuntimeException)
      assertEquals(NetworkError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get service request should match to the expected params`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { entityIdentity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val expectedURL =
      "$baseUrl$getServiceURL".replace(SERVICE_SID_PATH, factorServiceSid, true)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(identity),
          factorSid = eq(factorSid),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(FETCH),
          success = capture(),
          error = any()
        )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, {}, {})
    val requestCaptor = argumentCaptor<Request>().apply {
      verify(networkProvider).execute(capture(), any(), any())
    }

    requestCaptor.firstValue.apply {
      assertEquals(URL(expectedURL), url)
      assertEquals(Get, httpMethod)
      Assert.assertTrue(headers[ContentType.type] == UrlEncoded.type)
      Assert.assertTrue(headers[Accept.type] == UrlEncoded.type)
      Assert.assertTrue(headers.containsKey(AuthorizationHeader))
      Assert.assertTrue(headers.containsKey(userAgent))
      idlingResource.operationFinished()
    }
    idlingResource.waitForIdle()
  }
}