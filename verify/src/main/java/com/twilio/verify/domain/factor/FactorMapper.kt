/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.FactorDataPayload
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType.PUSH
import org.json.JSONException
import org.json.JSONObject

internal const val typeKey = "type"
internal const val statusKey = "status"
internal const val sidKey = "sid"
internal const val configKey = "config"
internal const val credentialSidKey = "credential_sid"
internal const val friendlyNameKey = "friendly_name"
internal const val accountSidKey = "account_sid"
internal const val serviceSidKey = "service_sid"
internal const val identity = "entity_identity"
internal const val keyPairAliasKey = "key_pair"
internal const val dateCreatedKey = "date_created"

internal class FactorMapper {

  @Throws(TwilioVerifyException::class)
  fun fromApi(
    jsonObject: JSONObject,
    factorPayload: FactorDataPayload
  ): Factor {
    val serviceSid = factorPayload.serviceSid
    val identity = factorPayload.identity
    if (serviceSid.isEmpty() || identity.isEmpty()) {
      throw TwilioVerifyException(
        IllegalArgumentException("ServiceSid or Identity is null or empty"), MapperError
      )
    }
    return when (factorPayload.type) {
      PUSH -> toPushFactor(serviceSid, identity, jsonObject)
    }
  }

  @Throws(TwilioVerifyException::class)
  fun status(
    jsonObject: JSONObject
  ): FactorStatus {
    return try {
      FactorStatus.values()
        .find { it.value == jsonObject.getString(statusKey) } ?: Unverified
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
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
    val identity = jsonObject.optString(identity)
    if (serviceSid.isNullOrEmpty() || identity.isNullOrEmpty()) {
      throw TwilioVerifyException(
        IllegalArgumentException("ServiceSid or Identity is null or empty"), MapperError
      )
    }
    return when (jsonObject.getString(typeKey)) {
      PUSH.factorTypeName ->
        toPushFactor(
          serviceSid,
          identity,
          jsonObject
        ).apply {
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
      PUSH ->
        JSONObject()
          .put(sidKey, factor.sid)
          .put(friendlyNameKey, factor.friendlyName)
          .put(accountSidKey, factor.accountSid)
          .put(serviceSidKey, factor.serviceSid)
          .put(identity, factor.identity)
          .put(typeKey, factor.type.factorTypeName)
          .put(keyPairAliasKey, (factor as PushFactor).keyPairAlias)
          .put(statusKey, factor.status.value)
          .put(
            configKey, JSONObject().put(credentialSidKey, factor.config.credentialSid)
          )
          .put(dateCreatedKey, toRFC3339Date(factor.createdAt))
          .toString()
    }
  }

  @Throws(TwilioVerifyException::class)
  private fun toPushFactor(
    serviceSid: String,
    identity: String,
    jsonObject: JSONObject
  ): PushFactor {
    return try {
      PushFactor(
        sid = jsonObject.getString(sidKey),
        friendlyName = jsonObject.getString(friendlyNameKey),
        accountSid = jsonObject.getString(accountSidKey),
        serviceSid = serviceSid,
        identity = identity,
        status = FactorStatus.values()
          .find { it.value == jsonObject.getString(statusKey) }
          ?: Unverified,
        createdAt = fromRFC3339Date(
          jsonObject.getString(dateCreatedKey)
        ),
        config = Config(
          jsonObject.getJSONObject(configKey)
            .getString(credentialSidKey)
        )
      )
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }
}
