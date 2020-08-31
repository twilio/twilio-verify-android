/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.cipher

import java.security.Key

data class CipherMockOutput(
  var cipherInitialized: Boolean = false,
  var secretKey: Key? = null,
  var encryptionTimes: MutableList<Long> = mutableListOf()
)
