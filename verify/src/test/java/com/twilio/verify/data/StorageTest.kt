/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val preferencesName = "testPreferences"

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StorageTest {

  private val sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
      .getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
  private val storage = Storage(sharedPreferences)

  @After
  fun tearDown() {
    ApplicationProvider.getApplicationContext<Context>()
        .deleteSharedPreferences(preferencesName)
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
}