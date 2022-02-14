/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  override val shouldExist: Boolean = true,
  override val authenticationRequired: Boolean = false
) : CipherTemplate() {
  override val algorithm = KeyProperties.KEY_ALGORITHM_AES
  override val keyGenParameterSpec: KeyGenParameterSpec = Builder(
    alias,
    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
  )
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
    .setKeySize(256)
    .setUserAuthenticationRequired(authenticationRequired)
    .build()
  override val cipherAlgorithm = "AES/GCM/NoPadding"
  override fun templateForCreation(): CipherTemplate = copy(shouldExist = false)
}
