package com.twilio.verify.networking

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.AuthenticationTokenError
import com.twilio.verify.data.DateProvider
import com.twilio.verify.data.getSignerTemplate
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import java.util.concurrent.TimeUnit
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val ctyKey = "cty"
internal const val kidKey = "kid"
internal const val jwtValidFor = 10L
internal const val subKey = "sub"
internal const val expKey = "exp"
internal const val iatKey = "nbf"
internal const val contentType = "twilio-pba;v=1"

internal class AuthenticationProvider(
  private val jwtGenerator: JwtGenerator,
  private val dateProvider: DateProvider
) : Authentication {

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
    put(ctyKey, contentType)
    put(kidKey, factor.config.credentialSid)
  }

  private fun generatePayload(factor: PushFactor) =
    JSONObject().apply {
      put(subKey, factor.accountSid)
      put(
        expKey,
        dateProvider.getCurrentTime() + TimeUnit.MINUTES.toSeconds(
          jwtValidFor
        )
      )
      put(iatKey, dateProvider.getCurrentTime())
    }
}
