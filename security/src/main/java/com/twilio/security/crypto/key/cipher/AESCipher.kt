/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.cipher

import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import javax.crypto.SecretKey

class AESCipher(
  internal val key: SecretKey,
  private val cipherAlgorithm: String,
  private val androidKeyStoreOperations: AndroidKeyStoreOperations
) : com.twilio.security.crypto.key.cipher.Cipher {
  override fun encrypt(data: ByteArray): EncryptedData {
    return try {
      return androidKeyStoreOperations.encrypt(data, cipherAlgorithm, key)
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  override fun decrypt(data: EncryptedData): ByteArray {
    return try {
      return androidKeyStoreOperations.decrypt(data, cipherAlgorithm, key)
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}
