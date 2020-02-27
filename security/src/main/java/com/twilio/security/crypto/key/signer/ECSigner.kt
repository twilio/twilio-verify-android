/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import com.twilio.security.crypto.KeyException
import java.security.KeyPair
import java.security.Signature

class ECSigner(
  internal val keyPair: KeyPair,
  private val signatureAlgorithm: String
) : Signer {
  @Throws(KeyException::class)
  override fun sign(data: ByteArray): ByteArray {
    return try {
      Signature.getInstance(signatureAlgorithm)
          .run {
            initSign(keyPair.private)
            update(data)
            sign()
          }
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
      Signature.getInstance(signatureAlgorithm)
          .run {
            initVerify(keyPair.public)
            update(data)
            verify(signature)
          }
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