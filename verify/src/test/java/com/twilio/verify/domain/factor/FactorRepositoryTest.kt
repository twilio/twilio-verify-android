/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
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
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.FactorType.PUSH
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
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class FactorRepositoryTest {

  private val apiClient: FactorAPIClient = mock()
  private val storage: StorageProvider = mock()
  private val factorMapper: FactorMapper = mock()
  private val factorRepository = FactorRepository(apiClient, storage, factorMapper)

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Create a factor with a valid factor builder should return a factor`() {
    val sid = "sid123"
    val factorPayload = CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123", emptyMap(),
        emptyMap(), "jwt"
    )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
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
    val factorPayload = CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123",
        emptyMap(), emptyMap(), "jwt"
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
    val factorPayload = CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123",
        emptyMap(), emptyMap(), "jwt"
    )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
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
    val factorPayload = CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123",
        emptyMap(), emptyMap(), "jwt"
    )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
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
    val updatedFactor = factorRepository.save(factor)
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
    factorRepository.save(factor)
  }

  @Test
  fun `Verify a factor with valid params should return a factor`() {
    val sid = "sid123"
    val factor = PushFactor(
        sid,
        "friendlyName",
        "accountSid",
        "serviceSid",
        "entityIdentity",
        FactorStatus.Unverified,
        Date(),
        Config("credentialSid")
    )
    val payload = "authPayload"
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid")
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
        "entityIdentity",
        FactorStatus.Unverified,
        Date(),
        Config("credentialSid")
    )
    val payload = "authPayload"
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid")
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

  @Test
  fun `Get all factors should return expected factors`() {
    val factor1: Factor = mock()
    val factor2: Factor = mock()
    val factorToJson1 = JSONObject().put(sidKey, "sid1")
        .toString()
    val factorToJson2 = JSONObject().put(sidKey, "sid2")
        .toString()

    val factorValues = listOf(factorToJson1, factorToJson2)

    whenever(factorMapper.fromStorage(factorToJson1)).thenReturn(factor1)
    whenever(factorMapper.fromStorage(factorToJson2)).thenReturn(factor2)
    whenever(storage.getAll()).thenReturn(factorValues)

    val factors = factorRepository.getAll()
    assertEquals(factorValues.size, factors.size)
    assertTrue(factors.contains(factor1))
    assertTrue(factors.contains(factor2))
  }

  @Test
  fun `Get all factors with a null response from mapper should filter not null values`() {
    val factorToJson1 = JSONObject().toString()
    val factorToJson2 = JSONObject().toString()

    val factorValues = listOf(factorToJson1, factorToJson2)

    whenever(factorMapper.fromStorage(factorToJson1)).thenReturn(null)
    whenever(factorMapper.fromStorage(factorToJson2)).thenReturn(null)
    whenever(storage.getAll()).thenReturn(factorValues)

    val factors = factorRepository.getAll()
    assertEquals(0, factors.size)
  }

  @Test
  fun `Update a factor with valid params should return a factor`() {
    val sidMock = "sid123"
    val updateFactorPayload = UpdateFactorPayload(
        "friendlyName",
        PUSH,
        "serviceSid",
        "entity",
        emptyMap(), sidMock
    )
    val response = JSONObject()
        .put(sidKey, sidMock)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid")
        .put(statusKey, FactorStatus.Unverified.value)

    val factorToJson = JSONObject().put(sidKey, sidMock)
        .toString()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.update(eq(updateFactorPayload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    val expectedFactor: Factor = mock {
      on { sid } doReturn (sidMock)
    }
    whenever(factorMapper.fromApi(response, updateFactorPayload)).thenReturn(expectedFactor)
    whenever(factorMapper.toJSON(expectedFactor)).thenReturn(factorToJson)
    whenever(storage.get(sidMock)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(expectedFactor)
    factorRepository.update(updateFactorPayload, { factor ->
      assertEquals(expectedFactor, factor)
    }, { fail() })
  }

  @Test
  fun `Error from mapper updating a factor should call error`() {
    val sidMock = "sid123"
    val updateFactorPayload = UpdateFactorPayload(
        "friendlyName",
        PUSH,
        "serviceSid",
        "entity",
        emptyMap(), sidMock
    )
    val response = JSONObject()
        .put(sidKey, sidMock)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid")
        .put(statusKey, FactorStatus.Unverified.value)
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.update(eq(updateFactorPayload), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    val expectedException: TwilioVerifyException = mock()
    whenever(factorMapper.fromApi(response, updateFactorPayload)).thenThrow(expectedException)
    factorRepository.update(updateFactorPayload, {
      fail()
    }, { exception -> assertEquals(expectedException, exception) })
  }

  @Test
  fun `Delete a factor should remove factor from storage`() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.delete(eq(factor), capture(), any())).then {
        firstValue.invoke()
      }
    }
    factorRepository.delete(factor, {
      verify(storage).remove(sid)
    }, { fail() })
  }

  @Test
  fun `Error deleting a factor with an API error should call error`() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(apiClient.delete(eq(factor), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    factorRepository.delete(factor, {
      fail()
    }, { assertEquals(expectedException, it) })
  }
}