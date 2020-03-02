/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey

class AESEncrypter(
  internal val key: SecretKey,
  private val cipherAlgorithm: String,
  private val parameterSpecClass: Class<out AlgorithmParameterSpec>
) : Encrypter {
  override fun encrypt(data: ByteArray): EncryptedData {
    return try {
      Cipher.getInstance(cipherAlgorithm)
          .run {
            init(Cipher.ENCRYPT_MODE, key)
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
            init(Cipher.DECRYPT_MODE, key, data.algorithmParameterSpec)
            doFinal(data.encrypted)
          }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}