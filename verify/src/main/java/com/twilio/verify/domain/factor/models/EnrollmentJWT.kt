/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import android.util.Base64
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import org.json.JSONException
import org.json.JSONObject

internal data class EnrollmentJWT(
  val jwt: String,
  val authyGrant: AuthyGrant
)

internal data class AuthyGrant(
  val serviceSid: String,
  val entity: String,
  val factorType: String
)

internal const val grantsKey = "grants"
internal const val authyGrantKey = "authy"
internal const val serviceSidKey = "service_sid"
internal const val entityIdentityKey = "entity_id"
internal const val factorKey = "factor"

@Throws(TwilioVerifyException::class)
internal fun toEnrollmentJWT(jwt: String): EnrollmentJWT {
  val parts = jwt.split(".")
  if (parts.size < 2) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT"), InputError)
  }
  val payloadJson = try {
    JSONObject(String(Base64.decode(parts[1], Base64.DEFAULT)))
  } catch (e: Exception) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT", e), InputError)
  }
  val authyGrantJson = try {
    payloadJson.getJSONObject(grantsKey)
        .getJSONObject(authyGrantKey)
  } catch (e: JSONException) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT", e), InputError)
  }
  val authyGrant = try {
    AuthyGrant(
        authyGrantJson.getString(serviceSidKey), authyGrantJson.getString(entityIdentityKey),
        authyGrantJson.getString(
            factorKey
        )
    )
  } catch (e: JSONException) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT", e), InputError)
  }
  return EnrollmentJWT(jwt, authyGrant)
}