/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.cipher

import java.security.AlgorithmParameters

data class CipherMockInput(
  var encrypted: String = "",
  var decrypted: String = "",
  var error: RuntimeException? = null,
  var algorithmParameters: AlgorithmParameters? = null,
  var delay: Int? = null
)
