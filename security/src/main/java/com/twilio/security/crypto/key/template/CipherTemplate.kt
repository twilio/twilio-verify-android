/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.template

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties

sealed class CipherTemplate : Template {
  internal abstract val keyGenParameterSpec: KeyGenParameterSpec
  internal abstract val cipherAlgorithm: String
  abstract fun templateForCreation(): CipherTemplate
}

data class AESGCMNoPaddingCipherTemplate(
  override val alias: String,
  override val shouldExist: Boolean = true
) : CipherTemplate() {
  override val algorithm = KeyProperties.KEY_ALGORITHM_AES
  override val keyGenParameterSpec: KeyGenParameterSpec = Builder(
      alias,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
  )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setKeySize(256)
      .build()
  override val cipherAlgorithm = "AES/GCM/NoPadding"
  override fun templateForCreation(): CipherTemplate = copy(shouldExist = false)
}