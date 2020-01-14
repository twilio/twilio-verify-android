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
internal const val userIdKey = "user_id"
internal const val keyPairAliasKey = "key_pair"

internal class FactorMapper {

  fun fromApi(
    jsonObject: JSONObject,
    factorBuilder: FactorBuilder
  ): Factor? {
    val serviceSid = factorBuilder.serviceSid
    val userId = factorBuilder.userId
    if (serviceSid.isNullOrEmpty() || userId.isNullOrEmpty()) {
      return null
    }
    return when (factorBuilder.type) {
      Push -> toPushFactor(serviceSid, userId, jsonObject)
      else -> null
    }
  }

  fun fromStorage(jsonObject: JSONObject): Factor? {
    val serviceSid = jsonObject.optString(serviceSidKey)
    val userId = jsonObject.optString(userIdKey)
    if (serviceSid.isNullOrEmpty() || userId.isNullOrEmpty()) {
      return null
    }
    return when (jsonObject.getString(typeKey)) {
      Push.name -> toPushFactor(serviceSid, userId, jsonObject)?.apply {
        keyPairAlias = jsonObject.getString(keyPairAliasKey)
      }
      else -> null
    }
  }

  fun toJSONObject(factor: Factor): JSONObject {
    return when (factor.type) {
      Push -> JSONObject()
          .put(sidKey, factor.sid)
          .put(friendlyNameKey, factor.friendlyName)
          .put(accountSidKey, factor.accountSid)
          .put(serviceSidKey, factor.serviceSid)
          .put(entitySidKey, factor.entitySid)
          .put(userIdKey, factor.userId)
          .put(typeKey, factor.type.name)
          .put(keyPairAliasKey, (factor as PushFactor).keyPairAlias)
    }
  }

  private fun toPushFactor(
    serviceSid: String,
    userId: String,
    jsonObject: JSONObject
  ): PushFactor? {
    return try {
      PushFactor(
          sid = jsonObject.getString(sidKey),
          friendlyName = jsonObject.getString(friendlyNameKey),
          accountSid = jsonObject.getString(accountSidKey),
          serviceSid = serviceSid,
          entitySid = jsonObject.getString(entitySidKey),
          userId = userId
      )
    } catch (e: JSONException) {
      null
    }
  }
}
