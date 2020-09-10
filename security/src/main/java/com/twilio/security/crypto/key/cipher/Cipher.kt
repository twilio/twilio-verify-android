/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.cipher

import com.twilio.security.crypto.KeyException

interface Cipher {
  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): EncryptedData

  @Throws(KeyException::class)
  fun decrypt(data: EncryptedData): ByteArray
}
