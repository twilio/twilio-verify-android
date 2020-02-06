/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.signature

data class SignatureMockInput(
  var signature: String = "",
  var error: RuntimeException? = null
)