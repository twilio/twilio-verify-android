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
import com.twilio.verify.api.FactorAPIClient
import com.twilio.verify.data.StorageProvider
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorRepositoryTest {

  private val apiClient: FactorAPIClient = mock()
  private val storage: StorageProvider = mock()
  private val factorMapper: FactorMapper = mock()
  private val authorization: Authorization = mock()
  private val factorRepository = FactorRepository(
      ApplicationProvider.getApplicationContext<Context>(), authorization, apiClient, storage,
      factorMapper
  )

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
    factorRepository.create(factorPayload) {
      assertEquals(factor, it)
    }
    verify(storage).save(sid, factorToJson)
  }

  @Test
  fun `No response from API creating a factor should not call success`() {
    val factorPayload = FactorPayload(
        "factor name", Push, mapOf("publicKey" to "value123"), "serviceSid123", "entityId123"
    )
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.create(eq(factorPayload), any(), capture())).then {
        firstValue.invoke()
      }
    }
    factorRepository.create(factorPayload) {
      fail()
    }
  }

  @Test
  fun `No factor from mapper creating a factor should not call success`() {
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
    whenever(factorMapper.fromApi(response, factorPayload)).thenReturn(null)
    factorRepository.create(factorPayload) {
      assertNull(it)
    }
  }

  @Test
  fun `No factor from storage creating a factor should not call success`() {
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
    factorRepository.create(factorPayload) {
      assertNull(it)
    }
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
  fun `Non existing factor in storage updating factor should return null`() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    whenever(storage.get(sid)).thenReturn(null)
    assertNull(factorRepository.update(factor))
  }
}