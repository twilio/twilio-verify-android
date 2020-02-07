/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import com.twilio.security.crypto.KeyException
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature

class ECSigner(
  internal val entry: PrivateKeyEntry,
  private val signatureAlgorithm: String
) : Signer {
  @Throws(KeyException::class)
  override fun sign(data: ByteArray): ByteArray {
    return try {
      Signature.getInstance(signatureAlgorithm)
          .run {
            initSign(entry.privateKey)
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
            initVerify(entry.certificate)
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
      entry.certificate.publicKey.encoded
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}