/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.domain.factor.models.FactorBuilder
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
  fun testFromApi_validData_shouldReturnFactor() {
    val factorBuilder = FactorBuilder().serviceSid("serviceSid123")
        .userId("userId123")
        .type(Push)
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    val factor = factorMapper.fromApi(jsonObject, factorBuilder) as PushFactor
    assertEquals(factorBuilder.type, factor.type)
    assertEquals(factorBuilder.serviceSid, factor.serviceSid)
    assertEquals(factorBuilder.userId, factor.userId)
    assertEquals(jsonObject.getString(sidKey), factor.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), factor.friendlyName)
    assertEquals(jsonObject.getString(accountSidKey), factor.accountSid)
    assertEquals(jsonObject.getString(entitySidKey), factor.entitySid)
  }

  @Test
  fun testFromApi_invalidData_shouldReturnNull() {
    val factorBuilder = FactorBuilder()
        .serviceSid("serviceSid123")
        .userId("userId123")
        .type(Push)
    val jsonObject = JSONObject()
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    assertNull(factorMapper.fromApi(jsonObject, factorBuilder))
  }

  @Test
  fun testFromApi_invalidServiceSid_shouldReturnNull() {
    val factorBuilder = FactorBuilder()
        .userId("userId123")
        .type(Push)
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    assertNull(factorMapper.fromApi(jsonObject, factorBuilder))
  }

  @Test
  fun testFromApi_invalidUserId_shouldReturnNull() {
    val factorBuilder = FactorBuilder()
        .serviceSid("serviceSid123")
        .type(Push)
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
    assertNull(factorMapper.fromApi(jsonObject, factorBuilder))
  }

  @Test
  fun testFromStorage_validData_shouldReturnFactor() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid123")
        .put(entitySidKey, "entitySid123")
        .put(userIdKey, "userId123")
        .put(typeKey, Push.name)
        .put(keyPairAliasKey, "keyPairAlias123")
    val factor = factorMapper.fromStorage(jsonObject.toString()) as PushFactor
    assertEquals(Push, factor.type)
    assertEquals(jsonObject.getString(serviceSidKey), factor.serviceSid)
    assertEquals(jsonObject.getString(userIdKey), factor.userId)
    assertEquals(jsonObject.getString(sidKey), factor.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), factor.friendlyName)
    assertEquals(jsonObject.getString(accountSidKey), factor.accountSid)
    assertEquals(jsonObject.getString(entitySidKey), factor.entitySid)
    assertEquals(jsonObject.getString(keyPairAliasKey), factor.keyPairAlias)
  }

  @Test
  fun testFromStorage_invalidData_shouldReturnNull() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
        .put(userIdKey, "userId123")
        .put(typeKey, Push.name)
        .put(keyPairAliasKey, "keyPairAlias123")
    assertNull(factorMapper.fromStorage(jsonObject.toString()))
  }

  @Test
  fun testFromStorage_invalidFactorType_shouldReturnNull() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(serviceSidKey, "serviceSid123")
        .put(entitySidKey, "entitySid123")
        .put(userIdKey, "userId123")
        .put(typeKey, "test")
        .put(keyPairAliasKey, "keyPairAlias123")
    assertNull(factorMapper.fromStorage(jsonObject.toString()))
  }

  @Test
  fun testFromStorage_invalidJson_shouldReturnNull() {
    val json = "test"
    assertNull(factorMapper.fromStorage(json))
  }

  @Test
  fun testToStorage_withFactorData_shouldReturnDataAsJsonObject() {
    val factor = PushFactor(
        sid = "sid123", friendlyName = "factor name", accountSid = "accountSid123",
        serviceSid = "serviceSid123", entitySid = "entitySid123", userId = "userId123"
    ).apply { keyPairAlias = "keyPairAlias123" }
    val json = factorMapper.toJSON(factor)
    val jsonObject = JSONObject(json)
    assertEquals(Push.name, jsonObject.getString(typeKey))
    assertEquals(factor.serviceSid, jsonObject.getString(serviceSidKey))
    assertEquals(factor.userId, jsonObject.getString(userIdKey))
    assertEquals(factor.sid, jsonObject.getString(sidKey))
    assertEquals(factor.friendlyName, jsonObject.getString(friendlyNameKey))
    assertEquals(factor.accountSid, jsonObject.getString(accountSidKey))
    assertEquals(factor.entitySid, jsonObject.getString(entitySidKey))
    assertEquals(factor.keyPairAlias, jsonObject.getString(keyPairAliasKey))
  }
}