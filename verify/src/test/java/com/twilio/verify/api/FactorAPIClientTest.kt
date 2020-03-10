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
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.publicKeyKey
import com.twilio.verify.domain.factor.pushTokenKey
import com.twilio.verify.models.FactorStatus.Unverified
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
import org.json.JSONObject
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

  private lateinit var factorAPIClient: FactorAPIClient
  private lateinit var networkProvider: NetworkProvider
  private lateinit var authorization: Authorization
  private lateinit var context: Context
  private val baseUrl = BuildConfig.BASE_URL

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    networkProvider = mock()
    authorization = Authorization("accountSid", "authToken")
    factorAPIClient = FactorAPIClient(networkProvider, context, authorization, baseUrl)
  }

  @Test
  fun `Create a factor with a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    factorAPIClient.create(
        FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entitySid123"),
        { jsonObject ->
          assertEquals(response, jsonObject.toString())
        }, {
      fail()
    })
  }

  @Test
  fun `Create a factor with an error response should call error`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    factorAPIClient.create(
        FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entitySid123"), {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Error creating a factor should call error`() {
    val factorPayload =
      FactorPayload("factor name", Push, emptyMap(), "serviceSid", "entitySid")
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
  fun `Create factor request should match to the expected params`() {
    val serviceSid = "serviceSid"
    val entity = "entityId"
    val expectedURL = "$baseUrl$createFactorURL".replace(serviceSidPath, serviceSid, true)
        .replace(
            entityPath, entity, true
        )
    val friendlyNameMock = "Test"
    val factorTypeMock = Push
    val pushToken = "ABCD"
    val publicKey = "12345"
    val expectedBody = mapOf(
        friendlyName to friendlyNameMock, factorType to factorTypeMock.factorTypeName,
        binding to JSONObject().apply {
          put(publicKeyKey, publicKey)
          put(algKey, defaultAlg)
        }.toString(),
        config to JSONObject().apply {
          put(sdkVersionKey, BuildConfig.VERSION_NAME)
          put(appIdKey, "${context.applicationInfo.loadLabel(context.packageManager)}")
          put(notificationPlatformKey, fcmPushType)
          put(notificationTokenKey, pushToken)
        }.toString()
    )
    val factorPayload =
      FactorPayload(
          friendlyNameMock, factorTypeMock,
          mapOf(pushTokenKey to pushToken, publicKeyKey to publicKey), serviceSid,
          entity
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

  @Test
  fun `Verify a factor with a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    factorAPIClient.verify(
        PushFactor(
            "sid",
            "friendlyName",
            "accountSid",
            "serviceSid",
            "entityIdentity",
            Unverified
        ),
        "authyPayload",
        { jsonObject ->
          assertEquals(response, jsonObject.toString())
        }, {
      fail()
    })
  }

  @Test
  fun `Verify a factor with an error response should not call success`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    factorAPIClient.verify(
        PushFactor(
            "sid",
            "friendlyName",
            "accountSid",
            "serviceSid",
            "entityIdentity",
            Unverified
        ),
        "authyPayload", {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Verify factor request should match to the expected params`() {
    val sidMock = "sid"
    val friendlyNameMock = "friendlyName"
    val accountSidMock = "accountSid"
    val serviceSidMock = "serviceSid"
    val entityIdentityMock = "entityIdentity"
    val authPayloadMock = "authPayload"
    val expectedURL = "$baseUrl$verifyFactorURL".replace(serviceSidPath, serviceSidMock, true)
        .replace(
            entityPath, entityIdentityMock, true
        )
        .replace(factorSidPath, sidMock)

    val expectedBody = mapOf(authPayloadParam to authPayloadMock)
    val factor =
      PushFactor(
          sidMock,
          friendlyNameMock,
          accountSidMock,
          serviceSidMock,
          entityIdentityMock,
          Unverified
      )

    factorAPIClient.verify(factor, authPayloadMock, {}, {})
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