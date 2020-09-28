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
      val rawValue = toByteArray(value)
      val encrypted = secretKeyProvider.encrypt(rawValue)
      preferences.edit()
        .putString(generateKeyDigest(key), Base64.encodeToString(encrypted, DEFAULT))
        .apply()
    } catch (e: Exception) {
      throw StorageException(e)
    }
  }

  @Throws(StorageException::class)
  override fun <T : Any> get(
    key: String,
    kClass: KClass<T>
  ): T {
    return try {
      getValue(generateKeyDigest(key), kClass) ?: throw IllegalArgumentException(
        "Illegal decrypted data"
      )
    } catch (e: Exception) {
      throw StorageException(e)
    }
  }

  @Throws(StorageException::class)
  override fun <T : Any> getAll(
    kClass: KClass<T>
  ): List<T> = try {
    preferences.all.filterValues { it is String }
      .mapNotNull { entry ->
        try {
          getValue(
            entry.key, kClass
          )
        } catch (e: Exception) {
          null
        }
      }
  } catch (e: Exception) {
    throw StorageException(e)
  }

  override fun contains(key: String): Boolean = preferences.contains(generateKeyDigest(key))

  @Synchronized
  override fun remove(key: String) {
    preferences.edit()
      .remove(generateKeyDigest(key))
      .apply()
  }

  @Synchronized
  override fun clear() {
    preferences.edit()
      .clear()
      .apply()
  }

  private fun <T : Any> getValue(
    key: String,
    kClass: KClass<T>
  ): T? {
    val value = preferences.getString(key, null) ?: throw IllegalArgumentException("key not found")
    return fromByteArray(secretKeyProvider.decrypt(Base64.decode(value, DEFAULT)), kClass)
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
  val messageDigest = MessageDigest.getInstance("SHA-256")
  return Base64.encodeToString(messageDigest.digest(key.toByteArray()), DEFAULT)
}
