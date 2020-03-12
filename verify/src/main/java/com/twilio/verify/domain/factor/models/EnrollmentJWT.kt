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
  val verifyConfig: VerifyConfig
)

internal data class VerifyConfig(
  val serviceSid: String,
  val entity: String,
  val factorType: String
)

internal const val grantsKey = "grants"
internal const val authyApiGrantKey = "api"
internal const val verifyGrantKey = "verify"
internal const val resourceKey = "res"
internal const val entityIdentityKey = "identity"
internal const val factorKey = "factor"
internal const val servicesPath = "/Services/"

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
  val authyApiGrantJson = try {
    payloadJson.getJSONObject(grantsKey)
        .getJSONObject(authyApiGrantKey)
        .getJSONArray("authy_v1")
        .getJSONObject(0)
  } catch (e: JSONException) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT", e), InputError)
  }
  val verifyGrantJson = try {
    payloadJson.getJSONObject(grantsKey)
        .getJSONObject(verifyGrantKey)
  } catch (e: JSONException) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT", e), InputError)
  }
  val verifyConfig = try {
    VerifyConfig(
        getServiceSid(authyApiGrantJson.getString(resourceKey)),
        verifyGrantJson.getString(entityIdentityKey),
        verifyGrantJson.getString(
            factorKey
        )
    )
  } catch (e: JSONException) {
    throw TwilioVerifyException(IllegalArgumentException("Invalid JWT", e), InputError)
  }
  return EnrollmentJWT(jwt, verifyConfig)
}

private fun getServiceSid(resource: String): String {
  return resource.substringAfter(servicesPath, "")
      .substringBefore('/', "").takeIf { it.isNotEmpty() } ?: throw TwilioVerifyException(
      IllegalArgumentException("Invalid service Sid"), InputError
  )
}
