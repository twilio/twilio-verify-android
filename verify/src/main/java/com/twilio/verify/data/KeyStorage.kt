/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

interface KeyStorage {
  fun create(alias: String): String
  fun sign(
    alias: String,
    message: String
  ): String
}