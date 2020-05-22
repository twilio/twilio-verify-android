/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.util.Base64.NO_WRAP

interface KeyStorage {
  fun create(alias: String): String
  fun sign(
    alias: String,
    message: String,
    flags: Int = NO_WRAP
  ): String

  fun delete(alias: String)
}