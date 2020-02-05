/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks

import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.KeyGeneratorSpi
import javax.crypto.SecretKey

internal const val keyGeneratorMockName =
  "com.twilio.security.crypto.mocks.KeyGeneratorMock"

class KeyGeneratorMock : KeyGeneratorSpi() {
  override fun engineInit(random: SecureRandom?) {

  }

  override fun engineInit(
    params: AlgorithmParameterSpec?,
    random: SecureRandom?
  ) {

  }

  override fun engineInit(
    keysize: Int,
    random: SecureRandom?
  ) {

  }

  override fun engineGenerateKey(): SecretKey? {
    keyStoreMockOutput.generatedKeyPair = true
    return keyStoreMockInput.key as? SecretKey
  }
}