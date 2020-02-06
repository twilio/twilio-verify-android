/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException
import java.security.spec.AlgorithmParameterSpec

interface Encrypter {
  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): EncryptedData

  @Throws(KeyException::class)
  fun decrypt(data: EncryptedData): ByteArray
}

data class EncryptedData(
  val algorithmParameterSpec: AlgorithmParameterSpec,
  val encrypted: ByteArray
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EncryptedData

    if (algorithmParameterSpec != other.algorithmParameterSpec) return false
    if (!encrypted.contentEquals(other.encrypted)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = algorithmParameterSpec.hashCode()
    result = 31 * result + encrypted.contentHashCode()
    return result
  }
}