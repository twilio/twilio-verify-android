/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.signature

import java.security.PrivateKey

data class SignatureMockOutput(
  var signatureInitialized: Boolean = false,
  var signatureDone: Boolean = false,
  var privateKey: PrivateKey? = null,
  var signatureUpdatedData: ByteArray? = null
)