/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import java.security.KeyStore.PrivateKeyEntry

class ECSigner(internal val entry: PrivateKeyEntry) : Signer {
  override fun sign(data: ByteArray): ByteArray {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }

  override fun verify(signature: ByteArray): Boolean {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }

  override fun getPublic(): ByteArray {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }
}