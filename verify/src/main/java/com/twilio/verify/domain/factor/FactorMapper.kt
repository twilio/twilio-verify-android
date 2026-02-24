/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.domain.factor

import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.FactorDataPayload
import com.twilio.verify.domain.factor.models.NotificationPlatform
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType.PUSH
import org.json.JSONException
import org.json.JSONObject

internal const val TYPE_KEY = "type"
internal const val STATUS_KEY = "status"
internal const val SID_KEY = "sid"
internal const val CONFIG_KEY = "config"
internal const val CREDENTIAL_SID_KEY = "credential_sid"
internal const val FRIENDLY_NAME_KEY = "friendly_name"
internal const val ACCOUNT_SID_KEY = "account_sid"
internal const val SERVICE_SID_KEY = "service_sid"
internal const val IDENTITY_KEY = "entity_identity"
internal const val KEY_PAIR_ALIAS_KEY = "key_pair"
internal const val DATE_CREATED_KEY = "date_created"
internal const val METADATA_KEY = "metadata"

internal class FactorMapper {
  @Throws(TwilioVerifyException::class)
  fun fromApi(
    jsonObject: JSONObject,
    factorPayload: FactorDataPayload,
  ): Factor {
    val serviceSid = factorPayload.serviceSid
    val identity = factorPayload.identity
    if (serviceSid.isEmpty() || identity.isEmpty()) {
      throw TwilioVerifyException(
        IllegalArgumentException("ServiceSid or Identity is null or empty").also { Logger.log(Level.Error, it.toString(), it) },
        MapperError,
      )
    }
    return when (factorPayload.type) {
      PUSH -> toPushFactor(serviceSid, identity, jsonObject)
    }
  }

  @Throws(TwilioVerifyException::class)
  fun status(jsonObject: JSONObject): FactorStatus =
    try {
      FactorStatus
        .values()
        .find { it.value == jsonObject.getString(STATUS_KEY) } ?: Unverified
    } catch (e: JSONException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    }

  @Throws(TwilioVerifyException::class)
  fun fromStorage(json: String): Factor {
    val jsonObject =
      try {
        JSONObject(json)
      } catch (e: JSONException) {
        Logger.log(Level.Error, e.toString(), e)
        throw TwilioVerifyException(e, MapperError)
      }
    val serviceSid = jsonObject.optString(SERVICE_SID_KEY)
    val identity = jsonObject.optString(IDENTITY_KEY)
    if (serviceSid.isNullOrEmpty() || identity.isNullOrEmpty()) {
      throw TwilioVerifyException(
        IllegalArgumentException("ServiceSid or Identity is null or empty").also { Logger.log(Level.Error, it.toString(), it) },
        MapperError,
      )
    }
    return when (jsonObject.getString(TYPE_KEY)) {
      PUSH.factorTypeName ->
        toPushFactor(
          serviceSid,
          identity,
          jsonObject,
        ).apply {
          keyPairAlias = jsonObject.optString(KEY_PAIR_ALIAS_KEY)
        }
      else -> throw TwilioVerifyException(
        IllegalArgumentException("Invalid factor type from json").also { Logger.log(Level.Error, it.toString(), it) },
        MapperError,
      )
    }
  }

  @Throws(TwilioVerifyException::class)
  fun toJSON(factor: Factor): String =
    when (factor.type) {
      PUSH ->
        JSONObject()
          .put(SID_KEY, factor.sid)
          .put(FRIENDLY_NAME_KEY, factor.friendlyName)
          .put(ACCOUNT_SID_KEY, factor.accountSid)
          .put(SERVICE_SID_KEY, factor.serviceSid)
          .put(IDENTITY_KEY, factor.identity)
          .put(TYPE_KEY, factor.type.factorTypeName)
          .put(KEY_PAIR_ALIAS_KEY, (factor as PushFactor).keyPairAlias)
          .put(STATUS_KEY, factor.status.value)
          .put(
            CONFIG_KEY,
            JSONObject()
              .put(CREDENTIAL_SID_KEY, factor.config.credentialSid)
              .put(NOTIFICATION_PLATFORM_KEY, factor.config.notificationPlatform.value),
          ).put(DATE_CREATED_KEY, toRFC3339Date(factor.createdAt))
          .apply {
            factor.metadata?.let {
              put(METADATA_KEY, JSONObject(it))
            }
          }.toString()
    }

  @Throws(TwilioVerifyException::class)
  private fun toPushFactor(
    serviceSid: String,
    identity: String,
    jsonObject: JSONObject,
  ): PushFactor =
    try {
      PushFactor(
        sid = jsonObject.getString(SID_KEY),
        friendlyName = jsonObject.getString(FRIENDLY_NAME_KEY),
        accountSid = jsonObject.getString(ACCOUNT_SID_KEY),
        serviceSid = serviceSid,
        identity = identity,
        status =
          FactorStatus
            .values()
            .find { it.value == jsonObject.getString(STATUS_KEY) }
            ?: Unverified,
        createdAt =
          fromRFC3339Date(
            jsonObject.getString(DATE_CREATED_KEY),
          ),
        config =
          Config(
            jsonObject.getJSONObject(CONFIG_KEY).getString(CREDENTIAL_SID_KEY),
            NotificationPlatform.values().find {
              it.value ==
                jsonObject.getJSONObject(CONFIG_KEY).optString(
                  NOTIFICATION_PLATFORM_KEY,
                )
            } ?: NotificationPlatform.FCM,
          ),
        metadata =
          jsonObject.optJSONObject(METADATA_KEY)?.let {
            it.keys().asSequence().associateWith { key -> it.getString(key) }
          },
      )
    } catch (e: JSONException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    }

  fun isFactor(json: String): Boolean {
    val jsonObject =
      try {
        JSONObject(json)
      } catch (e: JSONException) {
        return false
      }
    listOf(SERVICE_SID_KEY, IDENTITY_KEY, SID_KEY, ACCOUNT_SID_KEY).forEach {
      if (!jsonObject.has(it)) {
        return false
      }
    }
    return true
  }

  fun getSid(jsonObject: JSONObject): String = jsonObject.getString(SID_KEY)
}
