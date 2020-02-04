/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import com.twilio.security.crypto.KeyException

interface Signer {
  @Throws(KeyException::class)
  fun sign(data: ByteArray): ByteArray

  @Throws(KeyException::class)
  fun verify(signature: ByteArray): Boolean

  @Throws(KeyException::class)
  fun getPublic(): ByteArray
}