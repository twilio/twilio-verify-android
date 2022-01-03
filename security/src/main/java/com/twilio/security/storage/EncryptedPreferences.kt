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
import com.twilio.security.storage.key.SecretKeyProvider
import java.security.MessageDigest
import kotlin.reflect.KClass

class EncryptedPreferences(
  override val secretKeyProvider: SecretKeyProvider,
  private val preferences: SharedPreferences,
  override val serializer: Serializer
) : EncryptedStorage {
  @Throws(StorageException::class)
  @Synchronized
  override fun <T : Any> put(
    key: String,
    value: T
  ) {
    try {
      Logger.log(Level.Info, "Saving $key")
      val rawValue = toByteArray(value)
      val encrypted = secretKeyProvider.encrypt(rawValue)
      val keyToSave = generateKeyDigest(key)
      Logger.log(Level.Debug, "Saving $keyToSave")
      val result = preferences.edit()
        .putString(keyToSave, Base64.encodeToString(encrypted, DEFAULT))
        .commit()
      if (!result) {
        throw IllegalStateException("Error saving value")
      }
      Logger.log(Level.Debug, "Saved $keyToSave")
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
      throw StorageException(e)
    }
  }

  @Throws(StorageException::class)
  override fun <T : Any> get(
    key: String,
    kClass: KClass<T>
  ): T {
    return try {
      Logger.log(Level.Info, "Getting $key")
      getValue(generateKeyDigest(key), kClass) ?: throw IllegalArgumentException(
        "Illegal decrypted data"
      ).also { Logger.log(Level.Debug, "Return value $it for $key") }
    } catch (e: Exception) {
      Logger.log(Level.Error, e.toString(), e)
      throw StorageException(e)
    }
  }

  @Throws(StorageException::class)
  override fun <T : Any> getAll(
    kClass: KClass<T>
  ): List<T> = try {
    Logger.log(Level.Info, "Getting all values")
    preferences.all.filterValues { it is String }
      .mapNotNull { entry ->
        try {
          getValue(
            entry.key, kClass
          ).also { Logger.log(Level.Debug, "Return value $it for key ${entry.key}") }
        } catch (e: Exception) {
          Logger.log(Level.Error, e.toString(), e)
          null
        }
      }.also { Logger.log(Level.Info, "Return all values") }
  } catch (e: Exception) {
    Logger.log(Level.Error, e.toString(), e)
    throw StorageException(e)
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
    kClass: KClass<T>
  ): T? {
    Logger.log(Level.Debug, "Getting value for $key")
    val value = preferences.getString(key, null) ?: throw IllegalArgumentException("key not found")
    return fromByteArray(secretKeyProvider.decrypt(Base64.decode(value, DEFAULT)), kClass)
      .also { Logger.log(Level.Debug, "Return value $it for key $key") }
  }

  private fun <T : Any> toByteArray(
    value: T
  ): ByteArray = serializer.toByteArray(value)

  private fun <T : Any> fromByteArray(
    data: ByteArray,
    kClass: KClass<T>
  ): T? = serializer.fromByteArray(data, kClass)
}

internal fun generateKeyDigest(key: String): String {
  Logger.log(Level.Debug, "Generating key digest for $key")
  val messageDigest = MessageDigest.getInstance("SHA-256")
  return Base64.encodeToString(messageDigest.digest(key.toByteArray()), DEFAULT)
    .also { Logger.log(Level.Debug, "Generated key digest for $key: $it") }
}
