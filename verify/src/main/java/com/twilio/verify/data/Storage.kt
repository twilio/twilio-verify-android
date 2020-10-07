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
