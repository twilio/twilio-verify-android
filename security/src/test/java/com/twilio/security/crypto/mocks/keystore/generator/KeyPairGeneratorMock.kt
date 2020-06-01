package com.twilio.security.crypto.mocks.keystore.generator

import com.twilio.security.crypto.mocks.keystore.keyStoreMockInput
import com.twilio.security.crypto.mocks.keystore.keyStoreMockOutput
import java.lang.Thread.sleep
import java.security.KeyPair
import java.security.KeyPairGeneratorSpi
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.concurrent.TimeUnit

internal const val keyPairGeneratorMockName =
  "com.twilio.security.crypto.mocks.keystore.generator.KeyPairGeneratorMock"

class KeyPairGeneratorMock : KeyPairGeneratorSpi() {
  override fun generateKeyPair(): KeyPair? {
    keyStoreMockInput.delay?.let {
      sleep(TimeUnit.SECONDS.toMillis(it.toLong()))
    }
    synchronized(this) {
      keyStoreMockOutput.keyPairGenerationTimes.add(System.currentTimeMillis())
    }
    keyStoreMockOutput.generatedKeyPair = true
    keyStoreMockInput.containsAlias = true
    return keyStoreMockInput.newKey as? KeyPair
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