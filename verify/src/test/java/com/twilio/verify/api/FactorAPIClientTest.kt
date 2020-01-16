package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.domain.factor.models.FactorBuilder
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.HttpMethod
import com.twilio.verify.networking.MediaType
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
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
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
    factorAPIClient.create(FactorBuilder(), { jsonObject ->
      assertEquals(response, jsonObject.toString())
    }, {
      fail()
    })
  }

  @Test
  fun `API client with an error response should not call success`() {
    argumentCaptor<() -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke()
      }
    }
    factorAPIClient.create(FactorBuilder(), {
      fail()
    }, {})
  }

  @Test
  fun `Request should match to the expected params`() {
    val serviceSid = "serviceSid"
    val userId = "userId"
    val expectedURL = url.replace(serviceSidPath, serviceSid, true)
        .replace(
            userIdPath, userId, true
        )
    val friendlyNameMock = "Test"
    val factorTypeMock = Push
    val pushToken = "ABCD"
    val publicKey = "12345"
    val expectedBody = mapOf(
        friendlyName to friendlyNameMock, factorType to factorTypeMock.factorTypeName,
        binding to "$pushToken|$publicKey"
    )
    val factorBuilder =
      FactorBuilder().friendlyName(friendlyNameMock)
          .type(factorTypeMock)
          .binding(mapOf("pushToken" to pushToken, "publicKey" to publicKey))
          .serviceSid(serviceSid)
          .userId(userId)

    factorAPIClient.create(factorBuilder, {}, {})
    val requestCaptor = argumentCaptor<Request>().apply {
      verify(networkProvider).execute(capture(), any(), any())
    }
    requestCaptor.firstValue.apply {
      assertEquals(URL(expectedURL), url)
      assertEquals(HttpMethod.Post, httpMethod)
      assertEquals(expectedBody, body)
      assertTrue(headers[MediaType.ContentType.type] == MediaType.UrlEncoded.type)
      assertTrue(headers[MediaType.Accept.type] == MediaType.Json.type)
      assertTrue(headers.containsKey(AuthorizationHeader))
      assertTrue(headers.containsKey(userAgent))
    }
  }
}