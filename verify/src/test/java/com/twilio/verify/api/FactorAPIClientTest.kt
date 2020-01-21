package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.HttpMethod
import com.twilio.verify.networking.MediaTypeHeader
import com.twilio.verify.networking.MediaTypeValue
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
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
class FactorAPIClientTest {

  lateinit var factorAPIClient: FactorAPIClient
  lateinit var networkProvider: NetworkProvider
  lateinit var authorization: Authorization

  @Before
  fun setup() {
    val context: Context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    authorization = Authorization("accountSid", "authToken")
    factorAPIClient = FactorAPIClient(networkProvider, context, authorization)
  }

  @Test
  fun `API client a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    factorAPIClient.create(
        FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entityId123"),
        { jsonObject ->
          assertEquals(response, jsonObject.toString())
        }, {
      fail()
    })
  }

  @Test
  fun `API client with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    factorAPIClient.create(
        FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entityId123"), {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Error executing request should call error`() {
    val factorPayload = FactorPayload("factor name", Push, emptyMap(), "serviceSid", "entityId")
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    factorAPIClient.create(factorPayload, {
      fail()
    }, { exception ->
      assertTrue(exception.cause is NetworkException)
      assertTrue(exception.cause?.cause is RuntimeException)
      assertEquals(NetworkError.message, exception.message)
    })
  }

  @Test
  fun `Request should match to the expected params`() {
    val serviceSid = "serviceSid"
    val entityId = "userId"
    val expectedURL = url.replace(serviceSidPath, serviceSid, true)
        .replace(
            entityIdPath, entityId, true
        )
    val friendlyNameMock = "Test"
    val factorTypeMock = Push
    val pushToken = "ABCD"
    val publicKey = "12345"
    val expectedBody = mapOf(
        friendlyName to friendlyNameMock, factorType to factorTypeMock.factorTypeName,
        binding to "$pushToken|$publicKey"
    )
    val factorPayload =
      FactorPayload(
          friendlyNameMock, factorTypeMock,
          mapOf("pushToken" to pushToken, "publicKey" to publicKey), serviceSid,
          entityId
      )

    factorAPIClient.create(factorPayload, {}, {})
    val requestCaptor = argumentCaptor<Request>().apply {
      verify(networkProvider).execute(capture(), any(), any())
    }
    requestCaptor.firstValue.apply {
      assertEquals(URL(expectedURL), url)
      assertEquals(HttpMethod.Post, httpMethod)
      assertEquals(expectedBody, body)
      assertTrue(headers[MediaTypeHeader.ContentType.type] == MediaTypeValue.UrlEncoded.type)
      assertTrue(headers[MediaTypeHeader.Accept.type] == MediaTypeValue.Json.type)
      assertTrue(headers.containsKey(AuthorizationHeader))
      assertTrue(headers.containsKey(userAgent))
    }
  }
}