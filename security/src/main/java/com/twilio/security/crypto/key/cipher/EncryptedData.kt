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

package com.twilio.security.crypto.key.cipher

import android.util.Base64
import android.util.Base64.DEFAULT
import org.json.JSONObject

data class EncryptedData(
  val algorithmParameters: AlgorithmParametersSpec,
  val encrypted: ByteArray
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EncryptedData

    if (algorithmParameters != other.algorithmParameters) return false
    if (!encrypted.contentEquals(other.encrypted)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = algorithmParameters.hashCode()
    result = 31 * result + encrypted.contentHashCode()
    return result
  }
}

data class AlgorithmParametersSpec(
  val encoded: ByteArray,
  val provider: String,
  val algorithm: String
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AlgorithmParametersSpec

    if (!encoded.contentEquals(other.encoded)) return false
    if (provider != other.provider) return false
    if (algorithm != other.algorithm) return false

    return true
  }

  override fun hashCode(): Int {
    var result = encoded.contentHashCode()
    result = 31 * result + provider.hashCode()
    result = 31 * result + algorithm.hashCode()
    return result
  }
}

private const val encryptedKey = "encrypted"
private const val algorithmParametersKey = "algorithmParameters"
private const val encodedKey = "encoded"
private const val providerKey = "provider"
private const val algorithmKey = "algorithm"

internal fun fromByteArray(data: ByteArray): EncryptedData {
  val jsonObject = JSONObject(String(data))
  return EncryptedData(
    fromByteArray(jsonObject.getJSONObject(algorithmParametersKey)),
    Base64.decode(
      jsonObject.getString(
        encryptedKey
      ),
      DEFAULT
    )
  )
}

private fun fromByteArray(jsonObject: JSONObject): AlgorithmParametersSpec {
  return AlgorithmParametersSpec(
    Base64.decode(jsonObject.getString(encodedKey), DEFAULT),
    jsonObject.getString(
      providerKey
    ),
    jsonObject.getString(algorithmKey)
  )
}

internal fun toByteArray(encryptedData: EncryptedData): ByteArray = JSONObject()
  .apply {
    put(encryptedKey, Base64.encodeToString(encryptedData.encrypted, DEFAULT))
    put(algorithmParametersKey, toByteArray(encryptedData.algorithmParameters))
  }
  .toString()
  .toByteArray()

private fun toByteArray(algorithmParametersSpec: AlgorithmParametersSpec): JSONObject = JSONObject()
  .apply {
    put(encodedKey, Base64.encodeToString(algorithmParametersSpec.encoded, DEFAULT))
    put(providerKey, algorithmParametersSpec.provider)
    put(algorithmKey, algorithmParametersSpec.algorithm)
  }
