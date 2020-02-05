/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import java.security.KeyStore.SecretKeyEntry

class AESEncrypter(internal val entry: SecretKeyEntry) : Encrypter {
  override fun encrypt(data: ByteArray): ByteArray {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }

  override fun decrypt(data: ByteArray): ByteArray {
    TODO(
        "not implemented"
    ) //To change body of created functions use File | Settings | File Templates.
  }
}