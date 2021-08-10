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

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.authentication.BiometricAuthenticator
import com.twilio.security.crypto.key.authentication.BiometricError
import com.twilio.security.crypto.key.authentication.BiometricException
import com.twilio.security.crypto.key.cipher.fromByteArray
import com.twilio.security.crypto.key.cipher.toByteArray
import com.twilio.security.crypto.key.template.CipherTemplate
import java.security.InvalidKeyException

class BiometricSecretKey(
  val template: CipherTemplate,
  private val keyManager: KeyManager
) : AuthenticatedSecretKey {

  override fun create() {
    keyManager.cipher(template.templateForCreation())
  }

  override fun encrypt(data: ByteArray, authenticator: BiometricAuthenticator, success: (ByteArray) -> Unit, error: (Exception) -> Unit) {
    keyManager.cipher(template).encrypt(
      data, authenticator,
      {
        success(toByteArray(it))
      },
      { exception -> error(mapException(exception)) }
    )
  }

  override fun decrypt(data: ByteArray, authenticator: BiometricAuthenticator, success: (ByteArray) -> Unit, error: (Exception) -> Unit) {
    val encryptedData = fromByteArray(data)
    keyManager.cipher(template).decrypt(encryptedData, authenticator, success, { exception -> error(mapException(exception)) })
  }

  private fun mapException(exception: Exception): Exception {
    return when (exception) {
      is KeyPermanentlyInvalidatedException,
      is InvalidKeyException -> BiometricException(BiometricError.KeyInvalidated)
      else -> exception
    }
  }

  override fun delete() {
    keyManager.delete(template.alias)
  }
}
