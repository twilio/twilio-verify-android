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

package com.twilio.security.crypto

import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.CipherTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import java.security.KeyStore

interface KeyManager {
  @Throws(KeyException::class)
  fun signer(template: SignerTemplate): Signer

  @Throws(KeyException::class)
  fun cipher(template: CipherTemplate): Cipher

  @Throws(KeyException::class)
  fun delete(alias: String)

  fun contains(alias: String): Boolean
}

internal const val providerName = "AndroidKeyStore"

fun keyManager(): KeyManager =
  AndroidKeyManager(
    AndroidKeyStore(
      KeyStore.getInstance(providerName)
        .apply { load(null) }
    )
  )
