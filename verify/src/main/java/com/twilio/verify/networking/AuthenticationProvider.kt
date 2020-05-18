package com.twilio.verify.networking

import com.twilio.verify.models.Factor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val jwtValidFor = 5L
internal const val issKey = "iss"
internal const val subKey = "sub"
internal const val expKey = "exp"
internal const val iatKey = "iat"
internal const val grantsKey = "grants"
internal const val verifyPushKey = "verify_push"
internal const val factorSidKey = "factor_sid"
internal const val entitySidKey = "entity_sid"
internal const val serviceSidKey = "service_sid"

internal class AuthenticationProvider : Authentication {

  override fun generateJWT(
    factor: Factor
  ): String =
    generate(factor).toString()

  private fun generate(factor: Factor) =
    JSONObject().apply {
      put(issKey, factor.accountSid)
      put(subKey, factor.serviceSid)
      put(
          expKey, (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(
          jwtValidFor
      )) / 1000)
      put(iatKey, System.currentTimeMillis() / 1000)
      put(grantsKey, getGrants(factor))
    }

  private fun getGrants(factor: Factor) =
    JSONObject().apply {
      put(verifyPushKey, JSONObject().apply {
        put(factorSidKey, factor.sid)
        put(entitySidKey, factor.entityIdentity)
        put(serviceSidKey, factor.serviceSid)
      })
    }
}