/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

internal interface StorageProvider {

  fun save(
    key: String,
    value: String
  )

  fun get(key: String): String?

  fun getAll(): Collection<String>
  fun remove(key: String)
}
