package com.twilio.security.crypto.mocks.keystore

data class KeyStoreMockOutput(
  var generatedKeyPair: Boolean = false,
  var deletedAlias: String? = "",
  val keyPairGenerationTimes: MutableList<Long> = mutableListOf()
)