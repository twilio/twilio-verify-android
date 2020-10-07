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

import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.data.KeyStorage
import kotlin.math.max

private const val ES256_SIGNATURE_LENGTH = 64

internal class JwtSigner(private val keyStorage: KeyStorage) {

  fun sign(
    signerTemplate: SignerTemplate,
    content: String
  ): ByteArray {
    val signature = keyStorage.sign(signerTemplate.alias, content)
    return when (signerTemplate) {
      is ECP256SignerTemplate -> transcodeECSignatureToConcat(signature, ES256_SIGNATURE_LENGTH)
      else -> signature
    }
  }

  private fun transcodeECSignatureToConcat(
    derSignature: ByteArray,
    outputLength: Int
  ): ByteArray {
    if (derSignature.size < 8 || derSignature[0] != 48.toByte()) {
      throw IllegalArgumentException("Invalid ECDSA signature format")
    }
    val offset: Int = when {
      derSignature[1] > 0 -> {
        2
      }
      derSignature[1] == 0x81.toByte() -> {
        3
      }
      else -> {
        throw IllegalArgumentException("Invalid ECDSA signature format")
      }
    }
    val rLength = derSignature[offset + 1]
    var i = rLength.toInt()
    while (i > 0 && derSignature[offset + 2 + rLength - i] == 0.toByte()) {
      i--
    }
    val sLength = derSignature[offset + 2 + rLength + 1]
    var j = sLength.toInt()
    while (j > 0 && derSignature[offset + 2 + rLength + 2 + sLength - j] == 0.toByte()) {
      j--
    }
    var rawLen = max(i, j)
    rawLen = max(rawLen, outputLength / 2)
    if (derSignature[offset - 1].toInt()
      .and(0xff) != (derSignature.size - offset) ||
      derSignature[offset - 1].toInt()
        .and(0xff) != (2 + rLength + 2 + sLength) ||
      derSignature[offset] != 2.toByte() ||
      derSignature[offset + 2 + rLength] != 2.toByte()
    ) {
      throw IllegalArgumentException("Invalid ECDSA signature format")
    }
    val concatSignature = ByteArray(2 * rawLen)
    System.arraycopy(
      derSignature, offset + 2 + rLength - i, concatSignature, rawLen - i, i
    )
    System.arraycopy(
      derSignature, offset + 2 + rLength + 2 + sLength - j, concatSignature, 2 * rawLen - j, j
    )
    return concatSignature
  }
}
