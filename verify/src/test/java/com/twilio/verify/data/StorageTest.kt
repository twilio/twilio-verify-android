/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.storage.EncryptedStorage
import com.twilio.security.storage.StorageException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
  private val encryptedStorage: EncryptedStorage = mock()
  private val storage = Storage(sharedPreferences, encryptedStorage)

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
    verify(encryptedStorage).put(key, value)
  }

  @Test
  fun `Update a value should update it in preferences`() {
    val key = "key123"
    val value1 = "value123"
    val value2 = "value123"
    storage.save(key, value1)
    storage.save(key, value2)
    assertEquals(value2, sharedPreferences.getString(key, null))
    argumentCaptor<String>().apply {
      verify(encryptedStorage, times(2)).put(eq(key), capture())
      assertEquals(2, allValues.size)
      assertEquals(value1, firstValue)
      assertEquals(value2, secondValue)
    }
  }

  @Test
  fun `Get an existing value should return it from encrypted storage`() {
    val key = "key123"
    val value1 = "value123"
    val value2 = "value123"
    whenever(encryptedStorage.get(key, String::class)).thenReturn(value1)
    sharedPreferences.edit()
        .putString(key, value2)
        .apply()
    assertEquals(value1, storage.get(key))
  }

  @Test
  fun `Get an existing value should return it from preferences`() {
    val key = "key123"
    val value = "value123"
    whenever(encryptedStorage.get(key, String::class)).thenThrow(StorageException::class.java)
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
    verify(encryptedStorage).get(key, String::class)
  }

  @Test
  fun `Get all with saved factors should return all from encrypted storage`() {
    val expectedValues = listOf("value1", "value2")
    whenever(encryptedStorage.getAll(String::class)).thenReturn(expectedValues)
    val values = storage.getAll()
    assertEquals(expectedValues.size, values.size)
    expectedValues.forEach {
      assertTrue(values.contains(it))
    }
  }

  @Test
  fun `Get all with saved factors should return all from preferences`() {
    val expectedValues = mapOf("sid1" to "value1", "sid2" to "value2")
    expectedValues.forEach {
      storage.save(it.key, it.value)
      verify(encryptedStorage).put(it.key, it.value)
    }
    whenever(encryptedStorage.getAll(String::class)).thenThrow(StorageException::class.java)
    val values = storage.getAll()
    assertEquals(expectedValues.size, values.size)
    expectedValues.forEach {
      assertTrue(values.contains(it.value))
    }
  }

  @Test
  fun `Get all without any value saved should return 0`() {
    assertEquals(0, storage.getAll().size)
    verify(encryptedStorage).getAll(String::class)
  }

  @Test
  fun `Get all values with a list of not only strings should filter from preferences`() {
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
    verify(encryptedStorage).getAll(String::class)
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
    verify(encryptedStorage).remove(key)
  }
}