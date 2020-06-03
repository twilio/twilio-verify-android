package com.twilio.security.crypto.mocks.keystore

data class KeyStoreMockInput(
  var containsAlias: Boolean,
  val key: Any?,
  val newKey: Any? = null,
  val error: RuntimeException? = null,
  val delay: Int? = null
)