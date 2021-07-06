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
import android.util.Base64
import android.util.Base64.DEFAULT
import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.security.storage.key.BiometricSecretKey
import com.twilio.security.crypto.key.authentication.BiometricAuthenticator
import kotlin.reflect.KClass

class AuthenticatedEncryptedPreferences(
  override val biometricSecretKey: BiometricSecretKey,
  private val preferences: SharedPreferences,
  override val serializer: Serializer
) : AuthenticatedEncryptedStorage {

  @Throws(StorageException::class)
  @Synchronized
  override fun <T : Any> put(
    key: String,
    value: T,
    authenticator: BiometricAuthenticator, error: (Exception) -> Unit
  ) {
    try {
      Logger.log(Level.Info, "Saving $key")
      val rawValue = toByteArray(value)
      biometricSecretKey.encrypt(rawValue, authenticator, { encrypted ->
        val keyToSave = generateKeyDigest(key)
        Logger.log(Level.Debug, "Saving $keyToSave")
        val result = preferences.edit()
          .putString(keyToSave, Base64.encodeToString(encrypted, DEFAULT))
          .commit()
        if (!result) {
          throw IllegalStateException("Error saving value")
        }
        Logger.log(Level.Debug, "Saved $keyToSave")
      }, error)
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
      error(StorageException(e))
    }
  }

  @Throws(StorageException::class)
  override fun <T : Any> get(
    key: String,
    kClass: KClass<T>,
    authenticator: BiometricAuthenticator,
    success: (T) -> Unit,
    error: (Exception) -> Unit
  ) {
    return try {
      Logger.log(Level.Info, "Getting $key")
      getValue(generateKeyDigest(key), kClass, authenticator, {
        it?.let(success) ?: throw IllegalArgumentException(
          "Illegal decrypted data"
        )
      }, error).also { Logger.log(Level.Debug, "Return value $it for $key") }
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
      error(StorageException(e))
    }
  }

  override fun contains(key: String): Boolean = preferences.contains(generateKeyDigest(key))
    .also { Logger.log(Level.Debug, "Encrypted preferences ${if (it) "has a value" else "does not have a value"} for $it key $key") }

  @Synchronized
  override fun remove(key: String) {
    Logger.log(Level.Info, "Removing $key")
    preferences.edit()
      .remove(generateKeyDigest(key))
      .apply()
  }

  @Synchronized
  override fun clear() {
    Logger.log(Level.Info, "Clearing storage")
    preferences.edit()
      .clear()
      .apply()
  }

  private fun <T : Any> getValue(
    key: String,
    kClass: KClass<T>,
    authenticator: BiometricAuthenticator,
    success: (T?) -> Unit,
    error: (Exception) -> Unit
  ) {
    Logger.log(Level.Debug, "Getting value for $key")
    val value = preferences.getString(key, null) ?: throw IllegalArgumentException("key not found")
    biometricSecretKey.decrypt(Base64.decode(value, DEFAULT), authenticator, { decryptedValue ->
      success(fromByteArray(decryptedValue, kClass).also { Logger.log(Level.Debug, "Return value $it for key $key") })
    }, error)
  }

  private fun <T : Any> toByteArray(
    value: T
  ): ByteArray = serializer.toByteArray(value)

  private fun <T : Any> fromByteArray(
    data: ByteArray,
    kClass: KClass<T>
  ): T? = serializer.fromByteArray(data, kClass)
}
