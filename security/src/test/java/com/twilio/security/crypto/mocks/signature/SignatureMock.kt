/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.mocks.signature

import java.security.PrivateKey
import java.security.PublicKey
import java.security.SignatureSpi

internal const val signatureMockName = "com.twilio.security.crypto.mocks.signature.SignatureMock"

lateinit var signatureMockInput: SignatureMockInput
lateinit var signatureMockOutput: SignatureMockOutput

class SignatureMock : SignatureSpi() {
  override fun engineUpdate(b: Byte) {
    throw NotImplementedError()
  }

  override fun engineUpdate(
    b: ByteArray?,
    off: Int,
    len: Int
  ) {
    signatureMockOutput.signatureUpdatedData = b
  }

  override fun engineVerify(sigBytes: ByteArray?): Boolean {
    throw NotImplementedError()
  }

  override fun engineSign(): ByteArray {
    if (signatureMockInput.error != null) {
      throw signatureMockInput.error!!
    }
    signatureMockOutput.signatureDone = true
    return signatureMockInput.signature.toByteArray()
  }

  override fun engineGetParameter(param: String?): Any {
    throw NotImplementedError()
  }

  override fun engineInitVerify(publicKey: PublicKey?) {
    throw NotImplementedError()
  }

  override fun engineInitSign(privateKey: PrivateKey?) {
    signatureMockOutput.signatureInitialized = true
    signatureMockOutput.privateKey = privateKey
  }

  override fun engineSetParameter(
    param: String?,
    value: Any?
  ) {
    throw NotImplementedError()
  }
}