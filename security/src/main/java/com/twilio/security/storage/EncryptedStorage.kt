/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.SharedPreferences
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.security.storage.key.SecretKeyCipher
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

fun encryptedPreferences(
  storageAlias: String,
  sharedPreferences: SharedPreferences
): EncryptedStorage {
  val keyManager = keyManager()
  val secretKeyProvider = SecretKeyCipher(
    AESGCMNoPaddingCipherTemplate(storageAlias), keyManager
  )
  if (!keyManager.contains(storageAlias) && sharedPreferences.all.isEmpty()) {
    secretKeyProvider.create()
  }
  return EncryptedPreferences(secretKeyProvider, sharedPreferences, DefaultSerializer())
}
