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

package com.twilio.security.crypto.key.cipher

import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import javax.crypto.SecretKey

class AESCipher(
  internal val key: SecretKey,
  private val cipherAlgorithm: String,
  private val androidKeyStoreOperations: AndroidKeyStoreOperations
) : com.twilio.security.crypto.key.cipher.Cipher {
  override fun encrypt(data: ByteArray): EncryptedData {
    return try {
      return androidKeyStoreOperations.encrypt(data, cipherAlgorithm, key)
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }

  override fun decrypt(data: EncryptedData): ByteArray {
    return try {
      return androidKeyStoreOperations.decrypt(data, cipherAlgorithm, key)
    } catch (e: Exception) {
      throw KeyException(e)
    }
  }
}
