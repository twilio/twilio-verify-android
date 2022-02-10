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

package com.twilio.security.storage.key

import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.cipher.fromByteArray
import com.twilio.security.crypto.key.cipher.toByteArray
import com.twilio.security.crypto.key.template.CipherTemplate

class SecretKeyCipher(
  private val template: CipherTemplate,
  private val keyManager: KeyManager
) : EncryptionSecretKey {

  override fun create() {
    keyManager.cipher(template.templateForCreation())
  }

  override fun encrypt(data: ByteArray): ByteArray {
    return keyManager.cipher(template)
      .encrypt(data)
      .let {
        toByteArray(it)
      }
  }

  override fun decrypt(data: ByteArray): ByteArray {
    val encryptedData = fromByteArray(data)
    return keyManager.cipher(template)
      .decrypt(encryptedData)
  }

  override fun delete() {
    keyManager.delete(template.alias)
  }
}
