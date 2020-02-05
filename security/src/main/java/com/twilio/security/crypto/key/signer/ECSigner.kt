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
  override fun verify(signature: ByteArray): Boolean {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }

  @Throws(KeyException::class)
  override fun getPublic(): ByteArray {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }
}