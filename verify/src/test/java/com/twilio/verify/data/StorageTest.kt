/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.storage.EncryptedStorage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
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
  private lateinit var storage: Storage

  @Before
  fun setup() {
    sharedPreferences.edit()
      .putInt(CURRENT_VERSION, VERSION)
      .apply()
    storage = Storage(sharedPreferences, encryptedStorage, emptyList())
  }

  @After
  fun tearDown() {
    context.deleteSharedPreferences(preferencesName)
  }

  @Test
  fun `Save a new value should add it to encrypted storage`() {
    val key = "key123"
    val value = "value123"
    storage.save(key, value)
    verify(encryptedStorage).put(key, value)
  }

  @Test
  fun `Update a value should update it in encrypted storage`() {
    val key = "key123"
    val value1 = "value123"
    val value2 = "value345"
    storage.save(key, value1)
    storage.save(key, value2)
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
    whenever(encryptedStorage.get(key, String::class)).thenReturn(value1)
    assertEquals(value1, storage.get(key))
  }

  @Test
  fun `Get a non existing value should return null`() {
    val key = "key123"
    assertNull(storage.get(key))
    verify(encryptedStorage).get(key, String::class)
  }

  @Test
  fun `Get all with saved data should return all from encrypted storage`() {
    val expectedValues = listOf("value1", "value2")
    whenever(encryptedStorage.getAll(String::class)).thenReturn(expectedValues)
    val values = storage.getAll()
    assertEquals(expectedValues.size, values.size)
    expectedValues.forEach {
      assertTrue(values.contains(it))
    }
  }

  @Test
  fun `Get all without any value saved should return 0`() {
    assertEquals(0, storage.getAll().size)
    verify(encryptedStorage).getAll(String::class)
  }

  @Test
  fun `Remove a key should remove it from preferences`() {
    val key = "key123"
    storage.remove(key)
    verify(encryptedStorage).remove(key)
  }

  @Test
  fun `Migrations executed`() {
    val migrationV0ToV1: Migration = mock {
      on(it.startVersion).thenReturn(0)
      on(it.endVersion).thenReturn(1)
    }
    val migrationV1ToV2: Migration = mock {
      on(it.startVersion).thenReturn(1)
      on(it.endVersion).thenReturn(2)
    }
    val migrations = listOf(migrationV0ToV1, migrationV1ToV2)
    migration(0, 2, migrations)
    inOrder(migrationV0ToV1, migrationV1ToV2) {
      verify(migrationV0ToV1).migrate(any())
      verify(migrationV1ToV2).migrate(any())
    }
  }

  @Test
  fun `Migration executed`() {
    val migrationV0ToV1: Migration = mock {
      on(it.startVersion).thenReturn(0)
      on(it.endVersion).thenReturn(1)
    }
    val migrationV1ToV2: Migration = mock {
      on(it.startVersion).thenReturn(1)
      on(it.endVersion).thenReturn(2)
    }
    val migrations = listOf(migrationV0ToV1, migrationV1ToV2)
    migration(1, 2, migrations)
    verify(migrationV0ToV1, never()).migrate(any())
    verify(migrationV1ToV2).migrate(any())
  }

  @Test
  fun `No migration needed`() {
    val migrationV0ToV1: Migration = mock {
      on(it.startVersion).thenReturn(0)
      on(it.endVersion).thenReturn(1)
    }
    val migrationV1ToV2: Migration = mock {
      on(it.startVersion).thenReturn(1)
      on(it.endVersion).thenReturn(2)
    }
    val migrations = listOf(migrationV0ToV1, migrationV1ToV2)
    migration(2, 2, migrations)
    verify(migrationV0ToV1, never()).migrate(any())
    verify(migrationV1ToV2, never()).migrate(any())
  }

  @Test
  fun `Migrate data`() {
    val key = "key123"
    val value1 = "value123"
    val value2 = "value345"
    whenever(encryptedStorage.getAll(String::class)).thenReturn(listOf(value1))
    whenever(encryptedStorage.get(key, String::class)).thenReturn(value1)
    val migrationV1ToV2: Migration = mock {
      on(it.startVersion).thenReturn(1)
      on(it.endVersion).thenReturn(2)
      on(it.migrate(listOf(value1))).thenReturn(listOf(Entry(key, value2)))
    }
    val migrations = listOf(migrationV1ToV2)
    migration(1, 2, migrations)
    verify(encryptedStorage).put(key, value2)
  }

  private fun migration(
    startVersion: Int,
    endVersion: Int,
    migrations: List<Migration>
  ) {
    sharedPreferences.edit()
      .putInt(CURRENT_VERSION, startVersion)
      .apply()
    storage = Storage(sharedPreferences, encryptedStorage, migrations)
    assertEquals(endVersion, sharedPreferences.getInt(CURRENT_VERSION, 0))
  }
}
