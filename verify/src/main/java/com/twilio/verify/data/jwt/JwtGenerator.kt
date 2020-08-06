/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data.jwt

import android.util.Base64.NO_PADDING
import android.util.Base64.NO_WRAP
import android.util.Base64.URL_SAFE
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.data.encodeToBase64UTF8String
import com.twilio.verify.domain.factor.DEFAULT_ALG
import org.json.JSONObject

internal const val typeKey = "typ"
internal const val jwtType = "JWT"
internal const val ALGORITHM_KEY = "alg"
internal const val FLAGS = URL_SAFE or NO_PADDING or NO_WRAP

internal class JwtGenerator(private val jwtSigner: JwtSigner) {
  fun generateJWT(
    signerTemplate: SignerTemplate,
    header: JSONObject,
    payload: JSONObject
  ): String {
    header.put(typeKey, jwtType)
    when (signerTemplate) {
      is ECP256SignerTemplate -> header.put(ALGORITHM_KEY, DEFAULT_ALG)
    }
    val message = "${encodeToBase64UTF8String(
        header.toString()
            .toByteArray(), FLAGS
    )}.${encodeToBase64UTF8String(
        payload.toString()
            .toByteArray(), FLAGS
    )}"
    val signature = jwtSigner.sign(signerTemplate, message)
    return "$message.${encodeToBase64UTF8String(signature, FLAGS)}"
  }
}