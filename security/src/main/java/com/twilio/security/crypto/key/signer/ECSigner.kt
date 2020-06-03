/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import java.security.KeyPair

class ECSigner(
  internal val keyPair: KeyPair,
  private val signatureAlgorithm: String,
  private val androidKeyStoreOperations: AndroidKeyStoreOperations
) : Signer {
  @Throws(KeyException::class)
  override fun sign(data: ByteArray): ByteArray {
    return try {
      androidKeyStoreOperations.sign(data, signatureAlgorithm, keyPair.private)
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun verify(
    data: ByteArray,
    signature: ByteArray
  ): Boolean {
    return try {
      return androidKeyStoreOperations.verify(data, signature, signatureAlgorithm, keyPair.public)
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  @Throws(KeyException::class)
  override fun getPublic(): ByteArray {
    return try {
      keyPair.public.encoded
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}