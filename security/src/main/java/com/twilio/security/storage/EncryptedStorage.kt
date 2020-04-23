/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import com.twilio.security.storage.key.SecretKeyProvider
import java.io.Serializable
import kotlin.reflect.KClass

interface EncryptedStorage {
  val secretKeyProvider: SecretKeyProvider
  @Throws(StorageException::class)
  fun <T : Serializable> put(
    key: String,
    value: T
  )

  @Throws(StorageException::class)
  fun <T : Serializable> get(
    key: String,
    kClass: KClass<T>
  ): T

  @Throws(StorageException::class)
  fun <T : Serializable> getAll(kClass: KClass<T>): Map<String, T>

  fun contains(key: String): Boolean
  fun remove(key: String)
  fun clear()
}
