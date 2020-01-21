/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import android.util.Base64
import org.json.JSONException
import org.json.JSONObject

internal data class EnrollmentJWT(
  val jwt: String,
  val authyGrant: AuthyGrant
)

internal data class AuthyGrant(
  val serviceSid: String,
  val entityId: String,
  val factorType: String
)

internal const val grantsKey = "grants"
internal const val authyGrantKey = "authy"
internal const val serviceSidKey = "service_sid"
internal const val entityIdKey = "entity_id"
internal const val factorKey = "factor"

internal fun toEnrollmentJWT(jwt: String): EnrollmentJWT? {
  val parts = jwt.split(".")
  if (parts.size < 2) {
    return null
  }
  val payloadJson = try {
    JSONObject(String(Base64.decode(parts[1], Base64.DEFAULT)))
  } catch (e: Exception) {
    return null
  }
  val authyGrantJson = try {
    payloadJson.getJSONObject(grantsKey)
        .getJSONObject(authyGrantKey)
  } catch (e: JSONException) {
    return null
  }
  val authyGrant = try {
    AuthyGrant(
        authyGrantJson.getString(serviceSidKey), authyGrantJson.getString(entityIdKey),
        authyGrantJson.getString(
            factorKey
        )
    )
  } catch (e: JSONException) {
    return null
  }
  return EnrollmentJWT(jwt, authyGrant)
}