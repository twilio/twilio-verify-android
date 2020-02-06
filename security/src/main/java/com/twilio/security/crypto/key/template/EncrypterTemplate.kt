/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.template

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.spec.GCMParameterSpec

sealed class EncrypterTemplate : Template {
  internal abstract val keyGenParameterSpec: KeyGenParameterSpec
  internal abstract val cipherAlgorithm: String
  internal abstract val parameterSpecClass: Class<out AlgorithmParameterSpec>
}

data class AESGCMNoPaddingEncrypterTemplate(override val alias: String) : EncrypterTemplate() {
  override val algorithm = KeyProperties.KEY_ALGORITHM_AES
  override val keyGenParameterSpec: KeyGenParameterSpec = Builder(
      alias,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
  )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .build()
  override val cipherAlgorithm = "AES/GCM/NoPadding"
  override val parameterSpecClass: Class<out AlgorithmParameterSpec> = GCMParameterSpec::class.java
}