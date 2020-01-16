/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.domain.factor.models.FactorBuilder
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.Push
import org.json.JSONException
import org.json.JSONObject

internal const val typeKey = "type"
internal const val sidKey = "sid"
internal const val friendlyNameKey = "friendly_name"
internal const val accountSidKey = "account_sid"
internal const val serviceSidKey = "service_sid"
internal const val entitySidKey = "entity_sid"
internal const val entityIdKey = "user_id"
internal const val keyPairAliasKey = "key_pair"

internal class FactorMapper {

  fun fromApi(
    jsonObject: JSONObject,
    factorBuilder: FactorBuilder
  ): Factor? {
    val serviceSid = factorBuilder.serviceSid
    val entityId = factorBuilder.entityId
    if (serviceSid.isNullOrEmpty() || entityId.isNullOrEmpty()) {
      return null
    }
    return when (factorBuilder.type) {
      Push -> toPushFactor(serviceSid, entityId, jsonObject)
      else -> null
    }
  }

  fun fromStorage(json: String): Factor? {
    val jsonObject = try {
      JSONObject(json)
    } catch (e: JSONException) {
      return null
    }
    val serviceSid = jsonObject.optString(serviceSidKey)
    val entityId = jsonObject.optString(entityIdKey)
    if (serviceSid.isNullOrEmpty() || entityId.isNullOrEmpty()) {
      return null
    }
    return when (jsonObject.getString(typeKey)) {
      Push.factorTypeName -> toPushFactor(serviceSid, entityId, jsonObject)?.apply {
        keyPairAlias = jsonObject.getString(keyPairAliasKey)
      }
      else -> null
    }
  }

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
          .put(keyPairAliasKey, (factor as PushFactor).keyPairAlias).toString()
    }
  }

  private fun toPushFactor(
    serviceSid: String,
    entityId: String,
    jsonObject: JSONObject
  ): PushFactor? {
    return try {
      PushFactor(
          sid = jsonObject.getString(sidKey),
          friendlyName = jsonObject.getString(friendlyNameKey),
          accountSid = jsonObject.getString(accountSidKey),
          serviceSid = serviceSid,
          entitySid = jsonObject.getString(entitySidKey),
          entityId = entityId
      )
    } catch (e: JSONException) {
      null
    }
  }
}
