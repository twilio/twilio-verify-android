/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.signature

data class SignatureMockInput(
  var signature: String = "",
  var result: Boolean = false,
  var error: RuntimeException? = null,
  var delay: Int? = null
)