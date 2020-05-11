package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.BuildConfig
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.BasicAuthorization
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.MediaTypeHeader.Accept
import com.twilio.verify.networking.MediaTypeHeader.ContentType
import com.twilio.verify.networking.MediaTypeValue.UrlEncoded
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.userAgent
import org.junit.Assert
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
  private lateinit var authorization: Authentication
  private lateinit var context: Context
  private val baseUrl = BuildConfig.BASE_URL

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    authorization = BasicAuthorization("accountSid", "authToken")
    serviceAPIClient =
      ServiceAPIClient(networkProvider, context, authorization, baseUrl)
  }

  @Test
  fun `Get a service with a success response should call success`() {
    val response = "{\"sid\":\"serviceSid\",\"friendly_name\":\"friendlyName\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    serviceAPIClient.get("serviceSid", { jsonObject ->
      Assert.assertEquals(response, jsonObject.toString())
    }, {
      Assert.fail()
    })
  }

  @Test
  fun `Get a service with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    serviceAPIClient.get("serviceSid", {
      Assert.fail()
    }, { exception ->
      Assert.assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Error getting a service should call error`() {
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    serviceAPIClient.get("serviceSid", {
      Assert.fail()
    }, { exception ->
      Assert.assertTrue(exception.cause is NetworkException)
      Assert.assertTrue(exception.cause?.cause is RuntimeException)
      Assert.assertEquals(NetworkError.message, exception.message)
    })
  }

  @Test
  fun `Get service request should match to the expected params`() {
    val serviceSid = "sid"
    val expectedURL =
      "$baseUrl$getServiceURL".replace(SERVICE_SID_PATH, serviceSid, true)
    serviceAPIClient.get(serviceSid, {}, {})
    val requestCaptor = argumentCaptor<Request>().apply {
      verify(networkProvider).execute(capture(), any(), any())
    }

    requestCaptor.firstValue.apply {
      Assert.assertEquals(URL(expectedURL), url)
      Assert.assertEquals(Get, httpMethod)
      Assert.assertTrue(headers[ContentType.type] == UrlEncoded.type)
      Assert.assertTrue(headers[Accept.type] == UrlEncoded.type)
      Assert.assertTrue(headers.containsKey(AuthorizationHeader))
      Assert.assertTrue(headers.containsKey(userAgent))
    }
  }
}