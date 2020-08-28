/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.SharedPreferences
import com.twilio.security.storage.EncryptedStorage

internal const val CURRENT_VERSION = "cv"
internal const val VERSION = 2

internal class Storage(
  private val sharedPreferences: SharedPreferences,
  private val encryptedStorage: EncryptedStorage,
  private val migrations: List<Migration>
) : StorageProvider {

  override val version: Int = VERSION

  init {
    checkMigrations()
  }

  override fun save(
    key: String,
    value: String
  ) {
    encryptedStorage.put(key, value)
  }

  override fun get(key: String): String? = try {
    encryptedStorage.get(key, String::class)
  } catch (e: Exception) {
    null
  }

  override fun getAll(): List<String> = try {
    encryptedStorage.getAll(String::class)
        .takeIf { it.isNotEmpty() }
  } catch (e: Exception) {
    null
  } ?: emptyList()

  override fun remove(key: String) {
    encryptedStorage.remove(key)
  }

  private fun checkMigrations() {
    var currentVersion = sharedPreferences.getInt(CURRENT_VERSION, 1)
    if (currentVersion == version) {
      return
    }
    for (migration in migrations) {
      if (migration.startVersion < currentVersion) {
        continue
      }
      applyMigration(migration)
      currentVersion = migration.endVersion
      if (currentVersion == version) {
        break
      }
    }
  }

  private fun applyMigration(migration: Migration) {
    val migrationResult = migration.migrate(getAll())
    for (result in migrationResult) {
      save(result.key, result.newValue)
    }
    updateVersion(migration.endVersion)
  }

  private fun updateVersion(version: Int) {
    sharedPreferences.edit()
        .putInt(CURRENT_VERSION, version)
        .apply()
  }
}