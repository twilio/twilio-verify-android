/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class AESGCMEncrypter(
  internal val entry: SecretKeyEntry,
  private val cipherAlgorithm: String
) : Encrypter {
  override fun encrypt(data: ByteArray): EncryptedData {
    return try {
      Cipher.getInstance(cipherAlgorithm)
          .run {
            init(Cipher.ENCRYPT_MODE, entry.secretKey)
            EncryptedData(parameters.getParameterSpec(GCMParameterSpec::class.java), doFinal(data))
          }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  override fun decrypt(data: EncryptedData): ByteArray {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }
}