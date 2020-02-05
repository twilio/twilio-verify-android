/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.twilio.security.crypto.KeyException

interface Encrypter {
  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): ByteArray

  @Throws(KeyException::class)
  fun decrypt(data: ByteArray): ByteArray
}