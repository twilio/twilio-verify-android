/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.SharedPreferences

internal class Storage(private val sharedPreferences: SharedPreferences) : StorageProvider {
  override fun save(
    key: String,
    value: String
  ) {
    sharedPreferences.edit()
        .putString(key, value)
        .apply()
  }

  override fun get(key: String): String? = sharedPreferences.getString(key, null)

  override fun getAll(): Collection<String> =
    sharedPreferences.all.values.filterIsInstance<String>()
}