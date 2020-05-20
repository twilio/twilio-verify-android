/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.SharedPreferences
import com.twilio.security.storage.EncryptedStorage

internal class Storage(
  private val sharedPreferences: SharedPreferences,
  private val encryptedStorage: EncryptedStorage
) : StorageProvider {

  override fun save(
    key: String,
    value: String
  ) {
    sharedPreferences.edit()
        .putString(key, value)
        .apply()
    encryptedStorage.put(key, value)
  }

  override fun get(key: String): String? = try {
    encryptedStorage.get(key, String::class)
  } catch (e: Exception) {
    null
  } ?: sharedPreferences.getString(key, null)

  override fun getAll(): List<String> = try {
    encryptedStorage.getAll(String::class)
        .takeIf { it.isNotEmpty() }
  } catch (e: Exception) {
    null
  } ?: sharedPreferences.all.values.filterIsInstance<String>()

  override fun remove(key: String) {
    sharedPreferences.edit()
        .remove(key)
        .apply()
    encryptedStorage.remove(key)
  }
}