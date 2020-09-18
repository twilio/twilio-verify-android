/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

internal interface StorageProvider {

  val version: Int

  fun save(
    key: String,
    value: String
  )

  fun get(key: String): String?
  fun getAll(): List<String>
  fun remove(key: String)
}

interface Migration {
  val startVersion: Int
  val endVersion: Int

  fun migrate(data: List<String>): List<Entry>
}

data class Entry(val key: String, val newValue: String)
