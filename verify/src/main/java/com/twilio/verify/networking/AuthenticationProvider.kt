package com.twilio.verify.networking

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.AuthenticationTokenError
import com.twilio.verify.data.getSignerTemplate
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val typeKey = "typ"
internal const val ctyKey = "cty"
internal const val kidKey = "kid"
internal const val jwtValidFor = 10L
internal const val subKey = "sub"
internal const val expKey = "exp"
internal const val iatKey = "nbf"
internal const val grantsKey = "grants"
internal const val verifyPushKey = "verify_push"
internal const val factorSidKey = "factor_sid"
internal const val entitySidKey = "entity_sid"
internal const val serviceSidKey = "service_sid"
internal const val jwtType = "JWT"
internal const val contentType = "twilio-pba;v=1"

internal class AuthenticationProvider(private val jwtGenerator: JwtGenerator) : Authentication {

  override fun generateJWT(
    factor: Factor
  ): String {
    try {
      return when (factor) {
        is PushFactor -> generateJwt(factor)
        else -> throw IllegalArgumentException("Not supported factor for JWT generation")
      }
    } catch (e: Exception) {
      throw TwilioVerifyException(e, AuthenticationTokenError)
    }
  }

  private fun generateJwt(factor: PushFactor): String {
    val header = generateHeader(factor)
    val payload = generatePayload(factor)
    val alias = factor.keyPairAlias ?: throw IllegalStateException("Key pair not set")
    return jwtGenerator.generateJWT(getSignerTemplate(alias, true), header, payload)
  }

  private fun generateHeader(factor: PushFactor) = JSONObject().apply {
    put(typeKey, jwtType)
    put(ctyKey, contentType)
    put(kidKey, factor.config.credentialSid)
  }

  private fun generatePayload(factor: PushFactor) =
    JSONObject().apply {
      put(subKey, factor.accountSid)
      put(
          expKey, TimeUnit.MILLISECONDS.toSeconds(
          System.currentTimeMillis()
      ) + TimeUnit.MINUTES.toSeconds(
          jwtValidFor
      )
      )
      put(iatKey, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
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