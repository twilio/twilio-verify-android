package com.twilio.security.crypto.mocks.keystore

import java.security.KeyStore

data class KeyStoreMockInput(
  val containsAlias: Boolean,
  val entry: KeyStore.Entry?,
  val key: Any?,
  val error: RuntimeException? = null
)