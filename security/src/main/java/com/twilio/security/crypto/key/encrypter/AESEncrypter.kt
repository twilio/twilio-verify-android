/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException
import java.security.KeyStore.SecretKeyEntry
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher

class AESEncrypter(
  internal val entry: SecretKeyEntry,
  private val cipherAlgorithm: String,
  private val parameterSpecClass: Class<out AlgorithmParameterSpec>
) : Encrypter {
  override fun encrypt(data: ByteArray): EncryptedData {
    return try {
      Cipher.getInstance(cipherAlgorithm)
          .run {
            init(Cipher.ENCRYPT_MODE, entry.secretKey)
            EncryptedData(parameters.getParameterSpec(parameterSpecClass), doFinal(data))
          }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  override fun decrypt(data: EncryptedData): ByteArray {
    return try {
      Cipher.getInstance(cipherAlgorithm)
          .run {
            init(Cipher.DECRYPT_MODE, entry.secretKey, data.algorithmParameterSpec)
            doFinal(data.encrypted)
          }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}