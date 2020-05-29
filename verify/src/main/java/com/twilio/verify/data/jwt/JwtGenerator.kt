/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data.jwt

import android.util.Base64.NO_PADDING
import android.util.Base64.NO_WRAP
import android.util.Base64.URL_SAFE
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.data.encodeToUTF8String
import com.twilio.verify.domain.factor.DEFAULT_ALG
import org.json.JSONObject

internal const val ALGORITHM_KEY = "alg"
internal const val FLAGS = URL_SAFE or NO_PADDING or NO_WRAP

class JwtGenerator(private val jwtSigner: JwtSigner) {
  fun generateJWT(
    signerTemplate: SignerTemplate,
    header: JSONObject,
    payload: JSONObject
  ): String {
    when (signerTemplate) {
      is ECP256SignerTemplate -> header.put(ALGORITHM_KEY, DEFAULT_ALG)
    }
    val message = "${encodeToUTF8String(
        header.toString()
            .toByteArray(), FLAGS
    )}.${encodeToUTF8String(
        payload.toString()
            .toByteArray(), FLAGS
    )}"
    val signature = jwtSigner.sign(signerTemplate, message)
    return "$message.${encodeToUTF8String(signature, FLAGS)}"
  }
}