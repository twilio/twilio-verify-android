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

import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.key.authentication.Authenticator

interface Cipher {
  @Throws(KeyException::class)
  fun encrypt(data: ByteArray): EncryptedData

  @Throws(KeyException::class)
  fun decrypt(data: EncryptedData): ByteArray

  fun encrypt(data: ByteArray, authenticator: Authenticator, success: (EncryptedData) -> Unit, error: (Exception) -> Unit)
  fun decrypt(data: EncryptedData, authenticator: Authenticator, success: (ByteArray) -> Unit, error: (Exception) -> Unit)
}
