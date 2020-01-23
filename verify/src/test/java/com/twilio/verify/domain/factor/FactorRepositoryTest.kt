/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.StorageException
import com.twilio.verify.data.StorageProvider
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.FactorType.Push
import org.hamcrest.Matchers.instanceOf
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorRepositoryTest {

  private val apiClient: FactorAPIClient = mock()
  private val storage: StorageProvider = mock()
  private val factorMapper: FactorMapper = mock()
  private val factorRepository = FactorRepository(
      ApplicationProvider.getApplicationContext<Context>(), apiClient, storage,
      factorMapper
  )

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Create a factor with a valid factor builder should return a factor`() {
    val sid = "sid123"
    val factorPayload = FactorPayload(
        "factor name", Push, mapOf("publicKey" to "value123"), "serviceSid123", "entityId123"
    )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.create(eq(factorPayload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factorMapper.fromApi(response, factorPayload)).thenReturn(factor)
    whenever(factorMapper.toJSON(factor)).thenReturn(factorToJson)
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(factor)
    factorRepository.create(factorPayload, {
      assertEquals(factor, it)
    }, { fail() })
    verify(storage).save(sid, factorToJson)
  }

  @Test
  fun `No response from API creating a factor should call error`() {
    val factorPayload = FactorPayload(
        "factor name", Push, mapOf("publicKey" to "value123"), "serviceSid123", "entityId123"
    )
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(apiClient.create(eq(factorPayload), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    factorRepository.create(factorPayload, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception)
    })
  }

  @Test
  fun `Error from mapper creating a factor should call error`() {
    val sid = "sid123"
    val factorPayload = FactorPayload(
        "factor name", Push, mapOf("publicKey" to "value123"), "serviceSid123", "entityId123"
    )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.create(eq(factorPayload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    val expectedException: TwilioVerifyException = mock()
    whenever(factorMapper.fromApi(response, factorPayload)).thenThrow(expectedException)
    factorRepository.create(factorPayload, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception)
    })
  }

  @Test
  fun `No factor from storage creating a factor should call error`() {
    val sid = "sid123"
    val factorPayload = FactorPayload(
        "factor name", Push, mapOf("publicKey" to "value123"), "serviceSid123", "entityId123"
    )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.create(eq(factorPayload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factorMapper.fromApi(response, factorPayload)).thenReturn(factor)
    whenever(factorMapper.toJSON(factor)).thenReturn(factorToJson)
    whenever(storage.get(sid)).thenReturn(null)
    factorRepository.create(factorPayload, {
      fail()
    }, { exception ->
      assertTrue(exception.cause is StorageException)
    })
  }

  @Test
  fun `Get an existing factor should return it`() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(factor)
    val savedFactor = factorRepository.get(sid)
    assertEquals(factor, savedFactor)
  }

  @Test
  fun `Get a non existing factor should return null`() {
    val sid = "sid123"
    whenever(storage.get(sid)).thenReturn(null)
    assertNull(factorRepository.get(sid))
  }

  @Test
  fun `Get a null from mapper getting a factor should return null`() {
    val sid = "sid123"
    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(null)
    assertNull(factorRepository.get(sid))
  }

  @Test
  fun `Update a factor should return the updated factor`() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    val factorToUpdate = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    whenever(factorMapper.toJSON(factor)).thenReturn(factorToJson)
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(factorToUpdate)
    val updatedFactor = factorRepository.update(factor)
    assertEquals(factorToUpdate, updatedFactor)
    verify(storage).save(sid, factorToJson)
  }

  @Test
  fun `Non existing factor in storage updating factor should throw an exception`() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    whenever(storage.get(sid)).thenReturn(null)
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(
        instanceOf(
            StorageException::class.java
        )
    )
    exceptionRule.expect(ErrorCodeMatcher(StorageError))
    factorRepository.update(factor)
  }

  @Test
  fun `Verify a factor with valid params should return a factor`() {
    val sid = "sid123"
    val factor = PushFactor(
        sid,
        "friendlyName",
        "accountSid",
        "serviceSid",
        "entitySid",
        "entityId",
        FactorStatus.Unverified
    )
    val payload = "authPayload"
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid")
        .put(entitySidKey, "entitySid123")
        .put(entityIdKey, "entityId")
        .put(statusKey, FactorStatus.Unverified.value)

    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.verify(eq(factor), eq(payload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    val expectedFactorStatus = Verified
    whenever(factorMapper.status(response)).thenReturn(expectedFactorStatus)
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.toJSON(factor)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(factor)
    factorRepository.verify(factor, payload, {
      assertEquals(expectedFactorStatus, it.status)
      assertEquals(factor, it)
    }, { fail() })
    verify(storage).save(sid, factorToJson)
  }

  @Test
  fun `Error from mapper verifying a factor should call error`() {
    val sid = "sid123"
    val factor = PushFactor(
        sid,
        "friendlyName",
        "accountSid",
        "serviceSid",
        "entitySid",
        "entityId",
        FactorStatus.Unverified
    )
    val payload = "authPayload"
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid")
        .put(entitySidKey, "entitySid123")
        .put(entityIdKey, "entityId")
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.verify(eq(factor), eq(payload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    val expectedException: TwilioVerifyException = mock()
    whenever(factorMapper.status(response)).thenThrow(expectedException)
    factorRepository.verify(factor, payload, {
      fail()
    }, { exception -> assertEquals(expectedException, exception) })
  }
}