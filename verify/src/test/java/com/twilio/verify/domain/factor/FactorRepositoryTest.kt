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
import com.twilio.verify.domain.factor.models.FactorBuilder
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
  fun testCreate_validData_shouldReturnFactor() {
    val sid = "sid123"
    val factorBuilder = FactorBuilder().type(Push)
        .entityId("entityId123")
        .serviceSid("serviceSid123")
        .friendlyName("factor name")
        .binding(
            mapOf("publicKey" to "value123")
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
      whenever(apiClient.create(eq(factorBuilder), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factorMapper.fromApi(response, factorBuilder)).thenReturn(factor)
    whenever(factorMapper.toJSON(factor)).thenReturn(factorToJson)
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(factor)
    factorRepository.create(factorBuilder) {
      assertEquals(factor, it)
    }
    verify(storage).save(sid, factorToJson)
  }

  @Test
  fun testCreate_noResponseFromApi_shouldNotReturnFactor() {
    val factorBuilder = FactorBuilder().type(Push)
        .entityId("entityId123")
        .serviceSid("serviceSid123")
        .friendlyName("factor name")
        .binding(
            mapOf("publicKey" to "value123")
        )
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.create(eq(factorBuilder), any(), capture())).then {
        firstValue.invoke()
      }
    }
    factorRepository.create(factorBuilder) {
      fail()
    }
  }

  @Test
  fun testCreate_noFactorFromMapper_shouldNotReturnFactor() {
    val sid = "sid123"
    val factorBuilder = FactorBuilder().type(Push)
        .entityId("entityId123")
        .serviceSid("serviceSid123")
        .friendlyName("factor name")
        .binding(
            mapOf("publicKey" to "value123")
        )
    val response = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.create(eq(factorBuilder), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factorMapper.fromApi(response, factorBuilder)).thenReturn(null)
    factorRepository.create(factorBuilder) {
      assertNull(it)
    }
  }

  @Test
  fun testCreate_noFactorFromStorage_shouldNotReturnFactor() {
    val sid = "sid123"
    val factorBuilder = FactorBuilder().type(Push)
        .entityId("entityId123")
        .serviceSid("serviceSid123")
        .friendlyName("factor name")
        .binding(
            mapOf("publicKey" to "value123")
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
      whenever(apiClient.create(eq(factorBuilder), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factorMapper.fromApi(response, factorBuilder)).thenReturn(factor)
    whenever(factorMapper.toJSON(factor)).thenReturn(factorToJson)
    whenever(storage.get(sid)).thenReturn(null)
    factorRepository.create(factorBuilder) {
      assertNull(it)
    }
  }

  @Test
  fun testGet_savedFactor_shouldReturnFactor() {
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
  fun testGet_noFactorForSid_shouldReturnNull() {
    val sid = "sid123"
    whenever(storage.get(sid)).thenReturn(null)
    assertNull(factorRepository.get(sid))
  }

  @Test
  fun testGet_noFactorFromMapper_shouldReturnNull() {
    val sid = "sid123"
    val factorToJson = JSONObject().put(sidKey, sid)
        .toString()
    whenever(storage.get(sid)).thenReturn(factorToJson)
    whenever(factorMapper.fromStorage(factorToJson)).thenReturn(null)
    assertNull(factorRepository.get(sid))
  }

  @Test
  fun testUpdate_withFactor_shouldReturnUpdatedFactor() {
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
  fun testUpdate_noUpdatedFactorFound_shouldReturnNull() {
    val sid = "sid123"
    val factor = mock<Factor> {
      on(it.sid).thenReturn(sid)
    }
    whenever(storage.get(sid)).thenReturn(null)
    assertNull(factorRepository.update(factor))
  }
}