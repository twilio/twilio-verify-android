package com.twilio.sample.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/*
 * Copyright (c) 2020, Twilio Inc.
 */

private val sharedPreferencesName = "SampleApp"
private val urlKey = "url"

class LocalStorage(
  context: Context,
  private val preferences: SharedPreferences = context.getSharedPreferences(
      sharedPreferencesName, Context.MODE_PRIVATE
  )
) {
  var jwtURL: String
    get() = preferences.getString(urlKey, "") ?: ""
    set(value) = preferences.edit()
        .putString(urlKey, value)
        .apply()
}