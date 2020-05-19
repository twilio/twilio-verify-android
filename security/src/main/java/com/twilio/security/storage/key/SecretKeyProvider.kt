/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage.key

import com.twilio.security.crypto.KeyException

interface SecretKeyProvider {
  @Throws(KeyException::class)
  fun create()

  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): ByteArray

  @Throws(KeyException::class)
  fun decrypt(data: ByteArray): ByteArray

  @Throws(KeyException::class)
  fun delete()
}
