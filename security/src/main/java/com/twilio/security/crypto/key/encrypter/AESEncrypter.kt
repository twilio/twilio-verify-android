/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException
import java.security.AlgorithmParameters
import javax.crypto.Cipher
import javax.crypto.SecretKey

class AESEncrypter(
  internal val key: SecretKey,
  private val cipherAlgorithm: String
) : Encrypter {
  override fun encrypt(data: ByteArray): EncryptedData {
    return try {
      Cipher.getInstance(cipherAlgorithm)
          .run {
            init(Cipher.ENCRYPT_MODE, key)
            EncryptedData(
                AlgorithmParametersSpec(
                    parameters.encoded, parameters.provider.name,
                    parameters.algorithm
                )
                , doFinal(data)
            )
          }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  override fun decrypt(data: EncryptedData): ByteArray {
    return try {
      Cipher.getInstance(cipherAlgorithm)
          .run {
            val algorithmParameterSpec =
              AlgorithmParameters.getInstance(
                  data.algorithmParameters.algorithm, data.algorithmParameters.provider
              )
                  .apply {
                    init(data.algorithmParameters.encoded)
                  }
            init(Cipher.DECRYPT_MODE, key, algorithmParameterSpec)
            doFinal(data.encrypted)
          }
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}