package com.twilio.security.crypto.mocks

import java.security.KeyStore

data class KeyStoreMockInput(
  val containsAlias: Boolean,
  val entry: KeyStore.Entry?,
  val key: Any?,
  val error: RuntimeException? = null
)