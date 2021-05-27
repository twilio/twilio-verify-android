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

package com.twilio.security.storage

import android.content.SharedPreferences
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.keyManager
import com.twilio.security.storage.key.BiometricSecretKey
import com.twilio.security.storage.key.authentication.BiometricAuthenticator
import kotlin.reflect.KClass

interface SecureStorage {
  val storageAlias: String
  val keyManager: KeyManager
  val biometricSecretKey: BiometricSecretKey
  val serializer: Serializer

  @Throws(StorageException::class)
  fun <T : Any> put(
    key: String,
    value: T,
    authenticator: BiometricAuthenticator,
    error: (Exception) -> Unit
  )

  @Throws(StorageException::class)
  fun <T : Any> get(
    key: String,
    kClass: KClass<T>,
    authenticator: BiometricAuthenticator,
    success: (T) -> Unit,
    error: (Exception) -> Unit
  )

  fun contains(key: String): Boolean
  fun remove(key: String)
  fun clear()
}

fun securePreferences(
  storageAlias: String,
  sharedPreferences: SharedPreferences
): SecurePreferences {
  val keyManager = keyManager()
  return SecurePreferences(storageAlias, keyManager, sharedPreferences, DefaultSerializer())
}
