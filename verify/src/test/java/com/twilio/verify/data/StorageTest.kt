/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val preferencesName = "testPreferences"

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StorageTest {

  private val context: Context = ApplicationProvider.getApplicationContext()
  private val sharedPreferences =
    context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
  private val storage = Storage(sharedPreferences)

  @After
  fun tearDown() {
    context.deleteSharedPreferences(preferencesName)
  }

  @Test
  fun `Save a new value should add it to preferences`() {
    val key = "key123"
    val value = "value123"
    storage.save(key, value)
    assertEquals(value, sharedPreferences.getString(key, null))
  }

  @Test
  fun `Update a value should update it in preferences`() {
    val key = "key123"
    val value1 = "value123"
    val value2 = "value123"
    storage.save(key, value1)
    storage.save(key, value2)
    assertEquals(value2, sharedPreferences.getString(key, null))
  }

  @Test
  fun `Save a json string should save it correctly`() {
    val key = "key123"
    val value = JSONObject().apply {
      put("jKey1", "jValue1")
      put("jkey2", 123)
    }
    storage.save(key, value.toString())
    val jsonObject = JSONObject(sharedPreferences.getString(key, null))
    assertEquals(value.length(), jsonObject.length())
    assertEquals(value.getString("jKey1"), jsonObject.getString("jKey1"))
    assertEquals(value.getInt("jkey2"), jsonObject.getInt("jkey2"))
  }

  @Test
  fun `Get an existing value should return it`() {
    val key = "key123"
    val value = "value123"
    sharedPreferences.edit()
      .putString(key, value)
      .apply()
    assertEquals(value, storage.get(key))
  }

  @Test
  fun `Get a non existing value should return null`() {
    val key = "key123"
    sharedPreferences.edit()
      .remove(key)
      .apply()
    assertNull(storage.get(key))
  }

  @Test
  fun `Get all with saved factors should return all`() {
    val factors = mapOf("sid1" to "value1", "sid2" to "value2")
    factors.forEach { storage.save(it.key, it.value) }
    assertEquals(factors.size, sharedPreferences.all.size)
  }

  @Test
  fun `Get all without any value saved should return 0`() {
    assertEquals(0, sharedPreferences.all.size)
  }

  @Test
  fun `Get all values with a list of not only strings should filter`() {
    val keyValues = mapOf("sid1" to "value1", "sid2" to "value2", "sid3" to 123)
    keyValues.filter { it.value is String }
      .forEach {
        sharedPreferences.edit()
          .putString(it.key, it.value as String)
          .apply()
      }
    keyValues.filter { it.value is Int }
      .forEach {
        sharedPreferences.edit()
          .putInt(it.key, it.value as Int)
          .apply()
      }

    assertEquals(keyValues.size, sharedPreferences.all.size)
    assertEquals(keyValues.values.filterIsInstance<String>().size, storage.getAll().size)
  }

  @Test
  fun `Remove a key should remove it from preferences`() {
    val key = "key123"
    val value = "value123"
    sharedPreferences.edit()
      .putString(key, value)
      .apply()
    assertNotNull(storage.get(key))
    storage.remove(key)
    assertNull(storage.get(key))
    assertFalse(sharedPreferences.contains(key))
  }
}
