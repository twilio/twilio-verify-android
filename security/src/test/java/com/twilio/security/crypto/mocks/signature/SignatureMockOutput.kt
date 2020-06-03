/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.signature

import java.security.PrivateKey
import java.security.PublicKey

data class SignatureMockOutput(
  var initialized: Boolean = false,
  var privateKey: PrivateKey? = null,
  var publicKey: PublicKey? = null,
  var updatedData: ByteArray? = null,
  var signatureTimes: MutableList<Long> = mutableListOf()
)