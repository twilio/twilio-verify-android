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
        .toByteArray(),
      FLAGS
    )}.${encodeToBase64UTF8String(
      payload.toString()
        .toByteArray(),
      FLAGS
    )}"
    val signature = jwtSigner.sign(signerTemplate, message)
    return "$message.${encodeToBase64UTF8String(signature, FLAGS)}"
  }
}
