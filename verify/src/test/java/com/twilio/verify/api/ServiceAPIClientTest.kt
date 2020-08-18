package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.BuildConfig
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateProvider
import com.twilio.verify.models.Factor
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.FailureResponse
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.MediaTypeHeader.Accept
import com.twilio.verify.networking.MediaTypeHeader.ContentType
import com.twilio.verify.networking.MediaTypeValue.UrlEncoded
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.Response
import com.twilio.verify.networking.userAgent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
  private val dateProvider: DateProvider = mock()
  private lateinit var context: Context
  private val baseUrl = BuildConfig.BASE_URL
  private val idlingResource = IdlingResource()

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    serviceAPIClient =
      ServiceAPIClient(networkProvider, context, authentication, baseUrl, dateProvider)
  }

  @Test
  fun `Get a service with a success response should call success`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { this.identity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val response = "{\"sid\":\"serviceSid\",\"friendly_name\":\"friendlyName\"}"
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(Response(response, emptyMap()))
      }
    }
    whenever(authentication.generateJWT(factor)).thenReturn("authToken")
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
  fun `Get a service with an error response should call error`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { this.identity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val expectedException = NetworkException(
        FailureResponse(
            500,
            null,
            null
        )
    )
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    whenever(authentication.generateJWT(factor)).thenReturn("authToken")
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
  fun `Get a service with out of sync time request should sync time and redo the request`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { this.identity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }

    val date = "Tue, 21 Jul 2020 17:07:32 GMT"
    val expectedException = NetworkException(
        FailureResponse(
            unauthorized,
            null,
            mapOf(dateHeaderKey to listOf(date))
        )
    )
    val response = "{\"sid\":\"serviceSid\",\"friendly_name\":\"friendlyName\"}"
    argumentCaptor<(Response) -> Unit, (NetworkException) -> Unit>().let { (success, error) ->
      whenever(
          networkProvider.execute(any(), success.capture(), error.capture())
      ).then {
        error.firstValue.invoke(expectedException)
      }.then {
        success.firstValue.invoke(Response(response, emptyMap()))
      }
    }
    whenever(authentication.generateJWT(factor)).thenReturn("authToken")
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, { jsonObject ->
      assertEquals(response, jsonObject.toString())
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
    verify(dateProvider).syncTime(date)
  }

  @Test
  fun `Get a service with out of sync time should retry only another time`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { this.identity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }

    val date = "Tue, 21 Jul 2020 17:07:32 GMT"
    val expectedException = NetworkException(
        FailureResponse(
            unauthorized,
            null,
            mapOf(dateHeaderKey to listOf(date))
        )
    )
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        lastValue.invoke(expectedException)
      }
    }
    whenever(authentication.generateJWT(factor)).thenReturn("authToken")
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, { jsonObject ->
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
    verify(dateProvider).syncTime(date)
    verify(networkProvider, times(retryTimes + 1)).execute(any(), any(), any())
  }

  @Test
  fun `Error getting a service should call error`() {
    val identity = "identity"
    val factorSid = "sid"
    val factorServiceSid = "serviceSid"
    val factor: Factor = mock() {
      on { this.identity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is NetworkException)
      assertTrue(exception.cause?.cause is RuntimeException)
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
      on { this.identity } doReturn identity
      on { sid } doReturn factorSid
      on { serviceSid } doReturn factorServiceSid
    }
    val expectedURL =
      "$baseUrl$getServiceURL".replace(SERVICE_SID_PATH, factorServiceSid, true)
    whenever(authentication.generateJWT(factor)).thenReturn("authToken")
    idlingResource.startOperation()
    serviceAPIClient.get(factorServiceSid, factor, {}, {})
    val requestCaptor = argumentCaptor<Request>().apply {
      verify(networkProvider).execute(capture(), any(), any())
    }

    requestCaptor.firstValue.apply {
      assertEquals(URL(expectedURL), url)
      assertEquals(Get, httpMethod)
      assertTrue(headers[ContentType.type] == UrlEncoded.type)
      assertTrue(headers[Accept.type] == UrlEncoded.type)
      assertTrue(headers.containsKey(AuthorizationHeader))
      assertTrue(headers.containsKey(userAgent))
      idlingResource.operationFinished()
    }
    idlingResource.waitForIdle()
  }
}