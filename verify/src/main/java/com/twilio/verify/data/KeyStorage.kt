/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

internal interface KeyStorage {
  fun create(alias: String): String
  fun sign(
    alias: String,
    message: String
  ): ByteArray

  fun signAndEncode(
    alias: String,
    message: String
  ): String

  fun delete(alias: String)
}