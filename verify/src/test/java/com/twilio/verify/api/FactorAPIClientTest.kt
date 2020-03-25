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
import com.twilio.verify.domain.factor.ALG_KEY
import com.twilio.verify.domain.factor.APP_ID_KEY
import com.twilio.verify.domain.factor.DEFAULT_ALG
import com.twilio.verify.domain.factor.FCM_PUSH_TYPE
import com.twilio.verify.domain.factor.NOTIFICATION_PLATFORM_KEY
import com.twilio.verify.domain.factor.NOTIFICATION_TOKEN_KEY
import com.twilio.verify.domain.factor.PUBLIC_KEY_KEY
import com.twilio.verify.domain.factor.SDK_VERSION_KEY
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.AuthorizationHeader
import com.twilio.verify.networking.BasicAuthorization
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
    authorization = BasicAuthorization("accountSid", "authToken")
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
        CreateFactorPayload(
            "factor name", Push, "serviceSid123", "entitySid123", emptyMap(), emptyMap(), "jwt"
        ),
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
        CreateFactorPayload(
            "factor name", Push, "serviceSid123", "entitySid123", emptyMap(), emptyMap(), "jwt"
        ), {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Error creating a factor should call error`() {
    val factorPayload =
      CreateFactorPayload(
          "factor name", Push, "serviceSid", "entitySid", emptyMap(), emptyMap(), "jwt"
      )
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
    val expectedURL = "$baseUrl$CREATE_FACTOR_URL".replace(SERVICE_SID_PATH, serviceSid, true)
        .replace(
            ENTITY_PATH, entity, true
        )
    val friendlyNameMock = "Test"
    val factorTypeMock = Push
    val pushToken = "ABCD"
    val publicKey = "12345"
    val binding = mapOf(PUBLIC_KEY_KEY to publicKey, ALG_KEY to DEFAULT_ALG)
    val config = mapOf(
        SDK_VERSION_KEY to BuildConfig.VERSION_NAME,
        APP_ID_KEY to "${context.applicationInfo.loadLabel(context.packageManager)}",
        NOTIFICATION_PLATFORM_KEY to FCM_PUSH_TYPE,
        NOTIFICATION_TOKEN_KEY to pushToken
    )
    val expectedBody = mapOf(
        FRIENDLY_NAME_KEY to friendlyNameMock, FACTOR_TYPE_KEY to factorTypeMock.factorTypeName,
        BINDING_KEY to JSONObject(binding).toString(),
        CONFIG_KEY to JSONObject(config).toString()
    )

    val factorPayload =
      CreateFactorPayload(
          friendlyNameMock, factorTypeMock,
          serviceSid,
          entity, config, binding, "jwt"
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
    val expectedURL = "$baseUrl$VERIFY_FACTOR_URL".replace(SERVICE_SID_PATH, serviceSidMock, true)
        .replace(
            ENTITY_PATH, entityIdentityMock, true
        )
        .replace(FACTOR_SID_PATH, sidMock)

    val expectedBody = mapOf(AUTH_PAYLOAD_PARAM to authPayloadMock)
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

  fun `Update factor with a success response should call success`() {
    val response = "{\"key\":\"value\"}"
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    factorAPIClient.update(
        UpdateFactorPayload(
            "factor name", Push, "serviceSid123", "entitySid123", emptyMap(), "factorSid"
        ),
        { jsonObject ->
          assertEquals(response, jsonObject.toString())
        }, {
      fail()
    })
  }

  @Test
  fun `Update a factor with an error response shouldn't call success`() {
    val expectedException = NetworkException(500, null)
    argumentCaptor<(NetworkException) -> Unit>().apply {
      whenever(networkProvider.execute(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    factorAPIClient.update(
        UpdateFactorPayload(
            "factor name", Push, "serviceSid123", "entitySid123",
            emptyMap(), "factorSid"
        ), {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Update factor request should match to the expected params`() {
    val sidMock = "sid"
    val serviceSidMock = "serviceSid"
    val friendlyNameMock = "Test"
    val entityIdentityMock = "entityIdentity"
    val pushToken = "ABCD"
    val factorTypeMock = Push
    val expectedURL = "$baseUrl$UPDATE_FACTOR_URL".replace(SERVICE_SID_PATH, serviceSidMock, true)
        .replace(
            ENTITY_PATH, entityIdentityMock, true
        )
        .replace(FACTOR_SID_PATH, sidMock)

    val config = mapOf(
        SDK_VERSION_KEY to BuildConfig.VERSION_NAME,
        APP_ID_KEY to "${context.applicationInfo.loadLabel(context.packageManager)}",
        NOTIFICATION_PLATFORM_KEY to FCM_PUSH_TYPE,
        NOTIFICATION_TOKEN_KEY to pushToken
    )

    val factorPayload =
      UpdateFactorPayload(
          friendlyNameMock, factorTypeMock, serviceSidMock,
          entityIdentityMock, config, sidMock
      )

    val expectedBody = mapOf(
        FRIENDLY_NAME_KEY to friendlyNameMock,
        CONFIG_KEY to JSONObject(config).toString()
    )
    factorAPIClient.update(factorPayload, {}, {})
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