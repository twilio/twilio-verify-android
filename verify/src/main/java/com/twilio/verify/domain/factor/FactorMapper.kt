/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType.Push
import org.json.JSONException
import org.json.JSONObject

internal const val typeKey = "type"
internal const val statusKey = "status"
internal const val sidKey = "sid"
internal const val friendlyNameKey = "friendly_name"
internal const val accountSidKey = "account_sid"
internal const val serviceSidKey = "service_sid"
internal const val entitySidKey = "entity_sid"
internal const val entityIdKey = "user_id"
internal const val keyPairAliasKey = "key_pair"

internal class FactorMapper {

  @Throws(TwilioVerifyException::class)
  fun fromApi(
    jsonObject: JSONObject,
    factorPayload: FactorPayload
  ): Factor {
    val serviceSid = factorPayload.serviceSid
    val entityId = factorPayload.entityId
    if (serviceSid.isEmpty() || entityId.isEmpty()) {
      throw TwilioVerifyException(
          IllegalArgumentException("ServiceSid or EntityId is null or empty"), MapperError
      )
    }
    return when (factorPayload.type) {
      Push -> toPushFactor(serviceSid, entityId, jsonObject)
    }
  }

  @Throws(TwilioVerifyException::class)
  fun fromApi(
    factor: Factor,
    jsonObject: JSONObject
  ): Factor {
    return when (factor) {
      is PushFactor -> toPushFactor(jsonObject)
      else -> throw TwilioVerifyException(
          IllegalArgumentException("Invalid factor type"), MapperError
      )
    }
  }

  @Throws(TwilioVerifyException::class)
  fun fromStorage(json: String): Factor {
    val jsonObject = try {
      JSONObject(json)
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    }
    val serviceSid = jsonObject.optString(serviceSidKey)
    val entityId = jsonObject.optString(entityIdKey)
    if (serviceSid.isNullOrEmpty() || entityId.isNullOrEmpty()) {
      throw TwilioVerifyException(
          IllegalArgumentException("ServiceSid or EntityId is null or empty"), MapperError
      )
    }
    return when (jsonObject.getString(typeKey)) {
      Push.factorTypeName -> toPushFactor(serviceSid, entityId, jsonObject).apply {
        keyPairAlias = jsonObject.optString(keyPairAliasKey)
      }
      else -> throw TwilioVerifyException(
          IllegalArgumentException("Invalid factor type from json"), MapperError
      )
    }
  }

  @Throws(TwilioVerifyException::class)
  fun toJSON(factor: Factor): String {
    return when (factor.type) {
      Push -> JSONObject()
          .put(sidKey, factor.sid)
          .put(friendlyNameKey, factor.friendlyName)
          .put(accountSidKey, factor.accountSid)
          .put(serviceSidKey, factor.serviceSid)
          .put(entitySidKey, factor.entitySid)
          .put(entityIdKey, factor.entityId)
          .put(typeKey, factor.type.factorTypeName)
          .put(keyPairAliasKey, (factor as PushFactor).keyPairAlias)
          .put(statusKey, factor.status).toString()
    }
  }

  @Throws(TwilioVerifyException::class)
  private fun toPushFactor(
    serviceSid: String,
    entityId: String,
    jsonObject: JSONObject
  ): PushFactor {
    return try {
      PushFactor(
          sid = jsonObject.getString(sidKey),
          friendlyName = jsonObject.getString(friendlyNameKey),
          accountSid = jsonObject.getString(accountSidKey),
          serviceSid = serviceSid,
          entitySid = jsonObject.getString(entitySidKey),
          entityId = entityId,
          status = FactorStatus.values().find { it.value == jsonObject.getString(statusKey) }
              ?: Unverified
      )
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }

  @Throws(TwilioVerifyException::class)
  private fun toPushFactor(
    jsonObject: JSONObject
  ): PushFactor {
    return try {
      PushFactor(
          sid = jsonObject.getString(sidKey),
          friendlyName = jsonObject.getString(friendlyNameKey),
          accountSid = jsonObject.getString(accountSidKey),
          serviceSid = jsonObject.getString(serviceSidKey),
          entitySid = jsonObject.getString(entitySidKey),
          entityId = jsonObject.getString(entityIdKey),
          status = FactorStatus.values().find { it.value == jsonObject.getString(statusKey) }
              ?: Unverified
      )
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }
}
