/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encryptor

import com.twilio.security.crypto.KeyException

interface Encryptor {
  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): ByteArray

  @Throws(KeyException::class)
  fun decrypt(data: ByteArray): ByteArray
}