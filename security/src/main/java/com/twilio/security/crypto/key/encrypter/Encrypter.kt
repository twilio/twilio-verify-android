/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException
import java.io.Serializable

interface Encrypter {
  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): EncryptedData

  @Throws(KeyException::class)
  fun decrypt(data: EncryptedData): ByteArray
}

data class EncryptedData(
  val algorithmParameters: AlgorithmParametersSpec,
  val encrypted: ByteArray
): Serializable {
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
): Serializable {
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