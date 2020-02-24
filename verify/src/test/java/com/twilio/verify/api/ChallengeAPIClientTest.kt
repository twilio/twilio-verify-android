package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.ChallengeStatus.Pending
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
class ChallengeAPIClientTest {
  private lateinit var challengeAPIClient: ChallengeAPIClient
  private lateinit var networkProvider: NetworkProvider
  private lateinit var authorization: Authorization
  private lateinit var context: Context

  private val factorChallenge =
    FactorChallenge("sid", linkedMapOf(), linkedMapOf(), "factorSid", Pending).apply {
      factor =
        PushFactor("sid", "friendlyName", "accountSid", "serviceSid", "entitySid")
    }

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    authorization = Authorization("accountSid", "authToken")
    challengeAPIClient = ChallengeAPIClient(networkProvider, context, authorization)
  }

  @Test
  fun `Update a challenge with a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    challengeAPIClient.update(factorChallenge, "authPayload", {
    }, {
      fail()
    })
  }

  @Test
  fun `Update a challenge with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    challengeAPIClient.update(factorChallenge, "authPayload", {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Error updating a challenge should call error`() {
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    challengeAPIClient.update(factorChallenge, "authPayload", {
      fail()
    }, { exception ->
      assertTrue(exception.cause is NetworkException)
      assertTrue(exception.cause?.cause is RuntimeException)
      assertEquals(NetworkError.message, exception.message)
    })
  }

  @Test
  fun `Update a challenge with null factor should call error`() {
    val challenge = FactorChallenge("sid", linkedMapOf(), linkedMapOf(), "factorSid", Pending)
    challengeAPIClient.update(challenge, "authPayload", {
      fail()
    }, { exception ->
      assertTrue(exception.cause is NetworkException)
      assertTrue(exception.cause?.cause is IllegalArgumentException)
      assertEquals(NetworkError.message, exception.message)
    })
  }

  @Test
  fun `Update challenge request should match to the expected params`() {
    val expectedURL =
      updateChallengeURL.replace(serviceSidPath, factorChallenge.factor!!.serviceSid, true)
          .replace(
              entitySidPath, factorChallenge.factor!!.entitySid, true
          )
          .replace(factorSidPath, factorChallenge.factor!!.sid)
          .replace(challengeSidPath, factorChallenge.sid)

    val authPayload = "authPayload"
    val expectedBody = mapOf(
        authPayloadParam to authPayload
    )

    challengeAPIClient.update(factorChallenge, authPayload, {}, {})
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

  @Test
  fun `Get a challenge with a success response should call success`() {
    val factor =
      PushFactor("sid", "friendlyName", "accountSid", "serviceSid", "entitySid")
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    challengeAPIClient.get("sid", factor, { jsonObject ->
      assertEquals(response, jsonObject.toString())
    }, {
      fail()
    })
  }

  @Test
  fun `Get a challenge with an error response should call error`() {
    val factor =
      PushFactor("sid", "friendlyName", "accountSid", "serviceSid", "entitySid")
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    challengeAPIClient.get("sid", factor, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Error getting a challenge should call error`() {
    val factor =
      PushFactor("sid", "friendlyName", "accountSid", "serviceSid", "entitySid")
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    challengeAPIClient.get("sid", factor, {
      fail()
    }, { exception ->
      assertTrue(exception.cause is NetworkException)
      assertTrue(exception.cause?.cause is RuntimeException)
      assertEquals(NetworkError.message, exception.message)
    })
  }

  @Test
  fun `Get challenge request should match to the expected params`() {
    val challengeSid = "sid"
    val factor =
      PushFactor("sid", "friendlyName", "accountSid", "serviceSid", "entitySid")
    val expectedURL =
      getChallengeURL.replace(serviceSidPath, factor.serviceSid, true)
          .replace(
              entitySidPath, factor.entitySid, true
          )
          .replace(factorSidPath, factor.sid)
          .replace(challengeSidPath, challengeSid)

    challengeAPIClient.get(challengeSid, factor, {}, {})
    val requestCaptor = argumentCaptor<Request>().apply {
      verify(networkProvider).execute(capture(), any(), any())
    }

    requestCaptor.firstValue.apply {
      assertEquals(URL(expectedURL), url)
      assertEquals(HttpMethod.Get, httpMethod)
      assertTrue(headers[MediaTypeHeader.ContentType.type] == MediaTypeValue.UrlEncoded.type)
      assertTrue(headers[MediaTypeHeader.Accept.type] == MediaTypeValue.UrlEncoded.type)
      assertTrue(headers.containsKey(AuthorizationHeader))
      assertTrue(headers.containsKey(userAgent))
    }
  }
}