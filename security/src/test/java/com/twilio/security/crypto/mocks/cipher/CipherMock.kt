/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.cipher

import java.security.AlgorithmParameters
import java.security.AlgorithmParametersSpi
import java.security.Key
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.CipherSpi

internal const val cipherMockName = "com.twilio.security.crypto.mocks.cipher.CipherMock"
internal const val algorithmParametersMockName =
  "com.twilio.security.crypto.mocks.cipher.AlgorithmParametersMock"

lateinit var cipherMockInput: CipherMockInput
lateinit var cipherMockOutput: CipherMockOutput

class AlgorithmParametersMock : AlgorithmParametersSpi() {
  override fun engineGetEncoded(): ByteArray {
    throw NotImplementedError()
  }

  override fun engineGetEncoded(format: String?): ByteArray {
    throw NotImplementedError()
  }

  override fun engineInit(paramSpec: AlgorithmParameterSpec?) {
    throw NotImplementedError()
  }

  override fun engineInit(params: ByteArray?) {}

  override fun engineInit(
    params: ByteArray?,
    format: String?
  ) {
    throw NotImplementedError()
  }

  override fun engineToString(): String {
    throw NotImplementedError()
  }

  override fun <T : AlgorithmParameterSpec?> engineGetParameterSpec(paramSpec: Class<T>?): T {
    throw NotImplementedError()
  }

}

class CipherMock : CipherSpi() {
  override fun engineSetMode(mode: String?) {
    throw NotImplementedError()
  }

  override fun engineInit(
    opmode: Int,
    key: Key?,
    random: SecureRandom?
  ) {
    cipherMockOutput.cipherInitialized = true
    cipherMockOutput.secretKey = key
  }

  override fun engineInit(
    opmode: Int,
    key: Key?,
    params: AlgorithmParameterSpec?,
    random: SecureRandom?
  ) {
    cipherMockOutput.cipherInitialized = true
    cipherMockOutput.secretKey = key
  }

  override fun engineInit(
    opmode: Int,
    key: Key?,
    params: AlgorithmParameters?,
    random: SecureRandom?
  ) {
    cipherMockOutput.cipherInitialized = true
    cipherMockOutput.secretKey = key
  }

  override fun engineGetIV(): ByteArray {
    throw NotImplementedError()
  }

  override fun engineDoFinal(
    input: ByteArray?,
    inputOffset: Int,
    inputLen: Int
  ): ByteArray {
    if (cipherMockInput.error != null) {
      throw cipherMockInput.error!!
    }
    if (cipherMockInput.encrypted.isNotBlank()) {
      return cipherMockInput.encrypted.toByteArray()
    }
    return cipherMockInput.decrypted.toByteArray()
  }

  override fun engineDoFinal(
    input: ByteArray?,
    inputOffset: Int,
    inputLen: Int,
    output: ByteArray?,
    outputOffset: Int
  ): Int {
    throw NotImplementedError()
  }

  override fun engineSetPadding(padding: String?) {
    throw NotImplementedError()
  }

  override fun engineGetParameters(): AlgorithmParameters {
    return cipherMockInput.algorithmParameters!!
  }

  override fun engineUpdate(
    input: ByteArray?,
    inputOffset: Int,
    inputLen: Int
  ): ByteArray {
    throw NotImplementedError()
  }

  override fun engineUpdate(
    input: ByteArray?,
    inputOffset: Int,
    inputLen: Int,
    output: ByteArray?,
    outputOffset: Int
  ): Int {
    throw NotImplementedError()
  }

  override fun engineGetBlockSize(): Int {
    throw NotImplementedError()
  }

  override fun engineGetOutputSize(inputLen: Int): Int {
    throw NotImplementedError()
  }
}