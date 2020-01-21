/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.FactorType.Push
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorMapperTest {

  private val factorMapper = FactorMapper()

  @Test
  fun `Map a valid response from API should return a factor`() {
    val factorPayload =
      FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entityId123")
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    val factor = factorMapper.fromApi(jsonObject, factorPayload) as PushFactor
    assertEquals(factorPayload.type, factor.type)
    assertEquals(factorPayload.serviceSid, factor.serviceSid)
    assertEquals(factorPayload.entityId, factor.entityId)
    assertEquals(jsonObject.getString(sidKey), factor.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), factor.friendlyName)
    assertEquals(jsonObject.getString(accountSidKey), factor.accountSid)
    assertEquals(jsonObject.getString(entitySidKey), factor.entitySid)
  }

  @Test
  fun `Map an incomplete response from API should return null`() {
    val factorPayload =
      FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entityId123")
    val jsonObject = JSONObject()
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    assertNull(factorMapper.fromApi(jsonObject, factorPayload))
  }

  @Test
  fun `Map a response without factor sid from API should return null`() {
    val factorPayload =
      FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entityId123")
    val jsonObject = JSONObject()
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    assertNull(factorMapper.fromApi(jsonObject, factorPayload))
  }

  @Test
  fun `Map a response without entity sid from API should return null`() {
    val factorPayload =
      FactorPayload("factor name", Push, emptyMap(), "serviceSid123", "entityId123")
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid123")
    assertNull(factorMapper.fromApi(jsonObject, factorPayload))
  }

  @Test
  fun `Map a valid json from storage should return a factor`() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid123")
        .put(entitySidKey, "entitySid123")
        .put(entityIdKey, "entityId123")
        .put(typeKey, Push.factorTypeName)
        .put(keyPairAliasKey, "keyPairAlias123")
    val factor = factorMapper.fromStorage(jsonObject.toString()) as PushFactor
    assertEquals(Push, factor.type)
    assertEquals(jsonObject.getString(serviceSidKey), factor.serviceSid)
    assertEquals(jsonObject.getString(entityIdKey), factor.entityId)
    assertEquals(jsonObject.getString(sidKey), factor.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), factor.friendlyName)
    assertEquals(jsonObject.getString(accountSidKey), factor.accountSid)
    assertEquals(jsonObject.getString(entitySidKey), factor.entitySid)
    assertEquals(jsonObject.getString(keyPairAliasKey), factor.keyPairAlias)
  }

  @Test
  fun `Map an incomplete json from storage should return null`() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
        .put(entityIdKey, "entityId123")
        .put(typeKey, Push.factorTypeName)
        .put(keyPairAliasKey, "keyPairAlias123")
    assertNull(factorMapper.fromStorage(jsonObject.toString()))
  }

  @Test
  fun `Map an invalid factor type from storage should return null`() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid123")
        .put(entitySidKey, "entitySid123")
        .put(entityIdKey, "entityId123")
        .put(typeKey, "test")
        .put(keyPairAliasKey, "keyPairAlias123")
    assertNull(factorMapper.fromStorage(jsonObject.toString()))
  }

  @Test
  fun `Map an invalid json from storage should return null`() {
    val json = "test"
    assertNull(factorMapper.fromStorage(json))
  }

  @Test
  fun `Map a factor to JSON should return complete factor data as JSONObject`() {
    val factor = PushFactor(
        sid = "sid123", friendlyName = "factor name", accountSid = "accountSid123",
        serviceSid = "serviceSid123", entitySid = "entitySid123", entityId = "entityId123"
    ).apply { keyPairAlias = "keyPairAlias123" }
    val json = factorMapper.toJSON(factor)
    val jsonObject = JSONObject(json)
    assertEquals(Push.factorTypeName, jsonObject.getString(typeKey))
    assertEquals(factor.serviceSid, jsonObject.getString(serviceSidKey))
    assertEquals(factor.entityId, jsonObject.getString(entityIdKey))
    assertEquals(factor.sid, jsonObject.getString(sidKey))
    assertEquals(factor.friendlyName, jsonObject.getString(friendlyNameKey))
    assertEquals(factor.accountSid, jsonObject.getString(accountSidKey))
    assertEquals(factor.entitySid, jsonObject.getString(entitySidKey))
    assertEquals(factor.keyPairAlias, jsonObject.getString(keyPairAliasKey))
  }
}