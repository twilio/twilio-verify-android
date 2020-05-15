/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import com.twilio.security.storage.key.SecretKeyProvider
import kotlin.reflect.KClass

interface EncryptedStorage {
  val secretKeyProvider: SecretKeyProvider
  val serializer: Serializer

  @Throws(StorageException::class)
  fun <T : Any> put(
    key: String,
    value: T
  )

  @Throws(StorageException::class)
  fun <T : Any> get(
    key: String,
    kClass: KClass<T>
  ): T

  @Throws(StorageException::class)
  fun <T : Any> getAll(
    kClass: KClass<T>
  ): List<T>

  fun contains(key: String): Boolean
  fun remove(key: String)
  fun clear()
}
