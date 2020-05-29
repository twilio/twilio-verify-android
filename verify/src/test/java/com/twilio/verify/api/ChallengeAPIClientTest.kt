package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.BuildConfig
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.domain.challenge.factorSidKey
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.challenge.sidKey
import com.twilio.verify.domain.challenge.signatureFieldsHeaderSeparator
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.networking.Authentication
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.HttpMethod
import com.twilio.verify.networking.MediaTypeHeader
import com.twilio.verify.networking.MediaTypeValue
import com.twilio.verify.networking.NetworkException
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Request
import com.twilio.verify.networking.Response
import com.twilio.verify.networking.userAgent
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URL
import java.util.Date

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
class ChallengeAPIClientTest {
  private lateinit var challengeAPIClient: ChallengeAPIClient
  private lateinit var networkProvider: NetworkProvider
  private val authentication: Authentication = mock()
  private lateinit var context: Context
  private val baseUrl = BuildConfig.BASE_URL
  private val idlingResource = IdlingResource()

  private val factorChallenge =
    FactorChallenge("sid", mock(), "", "factorSid", Pending, Date(), Date(), Date()).apply {
      factor =
        PushFactor(
            "sid", "friendlyName", "accountSid", "serviceSid", "entityIdentity", createdAt = Date(),
            config = Config("credentialSid")
        )
    }

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    challengeAPIClient =
      ChallengeAPIClient(networkProvider, context, authentication, baseUrl)
  }

  @Test
  fun `Update a challenge with a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(Response(response, emptyMap()))
      }
    }
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.update(factorChallenge, "authPayload", {
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update a challenge with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.update(factorChallenge, "authPayload", {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error updating a challenge should call error`() {
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.update(factorChallenge, "authPayload", {
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
  fun `Update a challenge with a null factor should call error`() {
    val challenge = FactorChallenge("sid", mock(), "", "factorSid", Pending, Date(), Date(), Date())
    idlingResource.startOperation()
    challengeAPIClient.update(challenge, "authPayload", {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is NetworkException)
      assertTrue(exception.cause?.cause is IllegalArgumentException)
      assertEquals(NetworkError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge request should match to the expected params`() {
    val expectedURL =
      "$baseUrl$updateChallengeURL".replace(
          SERVICE_SID_PATH, factorChallenge.factor!!.serviceSid, true
      )
          .replace(
              ENTITY_PATH, factorChallenge.factor!!.entityIdentity, true
          )
          .replace(FACTOR_SID_PATH, factorChallenge.factor!!.sid)
          .replace(challengeSidPath, factorChallenge.sid)

    val authPayload = "authPayload"
    val expectedBody = mapOf(
        AUTH_PAYLOAD_PARAM to authPayload
    )
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
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
      idlingResource.operationFinished()
    }
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a challenge with a success response should call success`() {
    val expectedResponse = "{\"key\":\"value\"}"
    val expectedSignatureFieldsHeader =
      mapOf(
          signatureFieldsHeader to listOf(
              listOf(sidKey, factorSidKey).joinToString(
                  signatureFieldsHeaderSeparator
              )
          )
      )
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(Response(expectedResponse, expectedSignatureFieldsHeader))
      }
    }
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.get(
        "sid", factorChallenge.factor!!, { response: JSONObject, signatureFieldsHeader: String? ->
      assertEquals(expectedResponse, response.toString())
      assertEquals(
          expectedSignatureFieldsHeader.values.first()
              .first(), signatureFieldsHeader
      )
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a challenge with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.get("sid", factorChallenge.factor!!, { _: JSONObject, _: String? ->
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting a challenge should call error`() {
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    idlingResource.startOperation()
    challengeAPIClient.get("sid", factorChallenge.factor!!, { _: JSONObject, _: String? ->
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
  fun `Get challenge request should match to the expected params`() {
    val challengeSid = "sid"
    val factor = factorChallenge.factor!!
    val expectedURL =
      "$baseUrl$getChallengeURL".replace(SERVICE_SID_PATH, factor.serviceSid, true)
          .replace(
              ENTITY_PATH, factor.entityIdentity, true
          )
          .replace(FACTOR_SID_PATH, factor.sid)
          .replace(challengeSidPath, challengeSid)
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.get(challengeSid, factor, { _: JSONObject, _: String? -> }, {})
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
      idlingResource.operationFinished()
    }
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenges with a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(Response(response, emptyMap()))
      }
    }
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.getAll(factorChallenge.factor!!, null, 0, null, { jsonObject ->
      assertEquals(response, jsonObject.toString())
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenges with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    whenever(authentication.generateJWT(factorChallenge.factor!!)).thenReturn("authToken")
    idlingResource.startOperation()
    challengeAPIClient.getAll(factorChallenge.factor!!, null, 0, null, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting challenges should call error`() {
    whenever(networkProvider.execute(any(), any(), any())).thenThrow(RuntimeException())
    idlingResource.startOperation()
    challengeAPIClient.getAll(factorChallenge.factor!!, null, 0, null, {
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
}