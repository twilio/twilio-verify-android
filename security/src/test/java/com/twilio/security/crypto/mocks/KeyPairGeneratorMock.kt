package com.twilio.security.crypto.mocks

import java.security.KeyPair
import java.security.KeyPairGeneratorSpi
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec

internal const val keyPairGeneratorMockName =
  "com.twilio.security.crypto.mocks.KeyPairGeneratorMock"

class KeyPairGeneratorMock : KeyPairGeneratorSpi() {
  override fun generateKeyPair(): KeyPair? {
    keyStoreMockOutput.generatedKeyPair = true
    return keyStoreMockInput.key as? KeyPair
  }

  override fun initialize(
    keysize: Int,
    random: SecureRandom?
  ) {
  }

  override fun initialize(
    params: AlgorithmParameterSpec?,
    random: SecureRandom?
  ) {
  }
}