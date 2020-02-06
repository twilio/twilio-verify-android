package com.twilio.security.crypto.mocks.keystore.generator

import com.twilio.security.crypto.mocks.keystore.keyStoreMockInput
import com.twilio.security.crypto.mocks.keystore.keyStoreMockOutput
import java.security.KeyPair
import java.security.KeyPairGeneratorSpi
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec

internal const val keyPairGeneratorMockName =
  "com.twilio.security.crypto.mocks.keystore.generator.KeyPairGeneratorMock"

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