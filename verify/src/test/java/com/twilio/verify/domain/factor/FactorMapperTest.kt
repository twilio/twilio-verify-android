/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.FactorType.PUSH
import java.util.Date
import org.hamcrest.Matchers.instanceOf
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FactorMapperTest {

  private val factorMapper = FactorMapper()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Map a valid response from API with factorPayload should return a factor`() {
    val factorPayload =
      CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "identity123", emptyMap(), emptyMap(), "accessToken"
      )
    val metadata = mapOf("os" to "Android")
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(statusKey, Unverified.value)
      .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
      .put(dateCreatedKey, toRFC3339Date(Date()))
      .put(metadataKey, JSONObject(metadata))
    val factor = factorMapper.fromApi(jsonObject, factorPayload) as PushFactor
    assertEquals(factorPayload.type, factor.type)
    assertEquals(factorPayload.serviceSid, factor.serviceSid)
    assertEquals(factorPayload.identity, factor.identity)
    assertEquals(jsonObject.getString(sidKey), factor.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), factor.friendlyName)
    assertEquals(jsonObject.getString(accountSidKey), factor.accountSid)
    assertEquals(jsonObject.getString(statusKey), factor.status.value)
    assertEquals(jsonObject.getString(dateCreatedKey), toRFC3339Date(factor.createdAt))
    assertEquals(metadata, factor.metadata)
  }

  @Test
  fun `Map an incomplete response from API should throw an exception`() {
    val factorPayload =
      CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123", emptyMap(), emptyMap(), "accessToken"
      )
    val jsonObject = JSONObject()
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromApi(jsonObject, factorPayload)
  }

  @Test
  fun `Map a valid response from verifying a token API should return the factor status`() {
    val pushFactor = PushFactor("", "", "", "", "", Unverified, Date(), Config("credentialSid"))
    val jsonObject = JSONObject()
      .put(statusKey, Verified.value)
    pushFactor.status = factorMapper.status(jsonObject)
    assertEquals(jsonObject.getString(statusKey), pushFactor.status.value)
  }

  @Test
  fun `Map a response with invalid serviceSid in payload from API should throw an exception`() {
    val factorPayload =
      CreateFactorPayload("factor name", PUSH, "", "entitySid123", emptyMap(), emptyMap(), "accessToken")
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(IllegalArgumentException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromApi(jsonObject, factorPayload)
  }

  @Test
  fun `Map a response without factor sid from API should throw an exception`() {
    val factorPayload =
      CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123", emptyMap(), emptyMap(), "accessToken"
      )
    val jsonObject = JSONObject()
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromApi(jsonObject, factorPayload)
  }

  @Test
  fun `Map a response without entity sid from API should throw an exception`() {
    val factorPayload =
      CreateFactorPayload(
        "factor name", PUSH, "serviceSid123", "entitySid123", emptyMap(), emptyMap(), "accessToken"
      )
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(serviceSidKey, "serviceSid123")
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromApi(jsonObject, factorPayload)
  }

  @Test
  fun `Map a valid json from storage should return a factor`() {
    val metadata = mapOf("os" to "Android")
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(serviceSidKey, "serviceSid123")
      .put(identityKey, "identity123")
      .put(typeKey, PUSH.factorTypeName)
      .put(keyPairAliasKey, "keyPairAlias123")
      .put(statusKey, Unverified.value)
      .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
      .put(dateCreatedKey, toRFC3339Date(Date()))
      .put(metadataKey, JSONObject(metadata))
    val factor = factorMapper.fromStorage(jsonObject.toString()) as PushFactor
    assertEquals(PUSH, factor.type)
    assertEquals(jsonObject.getString(serviceSidKey), factor.serviceSid)
    assertEquals(jsonObject.getString(sidKey), factor.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), factor.friendlyName)
    assertEquals(jsonObject.getString(accountSidKey), factor.accountSid)
    assertEquals(jsonObject.getString(keyPairAliasKey), factor.keyPairAlias)
    assertEquals(jsonObject.getString(statusKey), factor.status.value)
    assertEquals(jsonObject.getString(dateCreatedKey), toRFC3339Date(factor.createdAt))
    assertEquals(metadata, factor.metadata)
  }

  @Test
  fun `Map an incomplete json from storage should throw an exception`() {
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(typeKey, PUSH.factorTypeName)
      .put(keyPairAliasKey, "keyPairAlias123")
      .put(identityKey, "identity123")
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(IllegalArgumentException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromStorage(jsonObject.toString())
  }

  @Test
  fun `Map an invalid factor type from storage should throw an exception`() {
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(serviceSidKey, "serviceSid123")
      .put(identityKey, "identity123")
      .put(typeKey, "test")
      .put(keyPairAliasKey, "keyPairAlias123")
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(IllegalArgumentException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromStorage(jsonObject.toString())
  }

  @Test
  fun `Map an invalid json from storage should throw an exception`() {
    val json = "test"
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    factorMapper.fromStorage(json)
  }

  @Test
  fun `Map a factor to JSON should return complete factor data as JSONObject`() {
    val factor = PushFactor(
      sid = "sid123", friendlyName = "factor name", accountSid = "accountSid123",
      serviceSid = "serviceSid123", identity = "identity123", status = Unverified,
      createdAt = Date(), config = Config("credentialSid")
    ).apply { keyPairAlias = "keyPairAlias123" }
    val json = factorMapper.toJSON(factor)
    val jsonObject = JSONObject(json)
    assertEquals(PUSH.factorTypeName, jsonObject.getString(typeKey))
    assertEquals(factor.serviceSid, jsonObject.getString(serviceSidKey))
    assertEquals(factor.sid, jsonObject.getString(sidKey))
    assertEquals(factor.friendlyName, jsonObject.getString(friendlyNameKey))
    assertEquals(factor.accountSid, jsonObject.getString(accountSidKey))
    assertEquals(factor.identity, jsonObject.getString(identityKey))
    assertEquals(factor.keyPairAlias, jsonObject.getString(keyPairAliasKey))
    assertEquals(toRFC3339Date(factor.createdAt), jsonObject.getString(dateCreatedKey))
  }

  @Test
  fun `Map a factor with metadata to JSON should return complete factor data as JSONObject`() {
    val metadata = mapOf("os" to "Android")
    val factor = PushFactor(
      sid = "sid123", friendlyName = "factor name", accountSid = "accountSid123",
      serviceSid = "serviceSid123", identity = "identity123", status = Unverified,
      createdAt = Date(), config = Config("credentialSid"), metadata = metadata
    ).apply { keyPairAlias = "keyPairAlias123" }
    val json = factorMapper.toJSON(factor)
    val jsonObject = JSONObject(json)
    assertEquals(PUSH.factorTypeName, jsonObject.getString(typeKey))
    assertEquals(JSONObject(metadata).toString(), jsonObject.getJSONObject(metadataKey).toString())
  }

  @Test
  fun `Evaluate is factor for valid json should return true`() {
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(serviceSidKey, "serviceSid123")
      .put(identityKey, "identity123")
      .put(typeKey, PUSH.factorTypeName)
      .put(keyPairAliasKey, "keyPairAlias123")
      .put(statusKey, Unverified.value)
      .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
      .put(dateCreatedKey, toRFC3339Date(Date()))
    assertTrue(factorMapper.isFactor(jsonObject.toString()))
  }

  @Test
  fun `Evaluate is factor for incomplete json should return false`() {
    val jsonObject = JSONObject()
      .put(sidKey, "sid123")
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(typeKey, PUSH.factorTypeName)
      .put(keyPairAliasKey, "keyPairAlias123")
      .put(identityKey, "identity123")
    assertFalse(factorMapper.isFactor(jsonObject.toString()))
  }
}
