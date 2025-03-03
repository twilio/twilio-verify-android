/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Base64
import com.twilio.security.storage.key.EncryptionSecretKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.reflect.KClass
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EncryptedPreferencesTest {

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val preferences: SharedPreferences = mockk()
  private val secretKeyProvider: EncryptionSecretKey = mockk(relaxed = true)
  private val serializer: Serializer = mockk(relaxed = true)
  private lateinit var encryptedPreferences: EncryptedPreferences

  @Before
  fun setup() {
    encryptedPreferences = EncryptedPreferences(secretKeyProvider, preferences, serializer)
  }

  @Test
  fun testPut_withValue_shouldSaveEncryptedValue() {
    val key = "key"
    val value = "value"
    val editor: SharedPreferences.Editor = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()
    every { secretKeyProvider.encrypt(any()) } returns value.toByteArray()
    every { editor.putString(eq(generateKeyDigest(key)), any()) } returns editor
    every { editor.commit() } returns true

    encryptedPreferences.put(key, value)

    verify {
      editor.putString(
        generateKeyDigest(key), Base64.encodeToString(value.toByteArray(), Base64.DEFAULT)
      )
      editor.commit()
    }
  }

  @Test
  fun testPut_withError_shouldThrowError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mockk()
    val exception: RuntimeException = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()
    every { secretKeyProvider.encrypt(any()) } throws exception

    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(
      instanceOf<Throwable>(
        RuntimeException::class.java
      )
    )
    encryptedPreferences.put(key, value)
  }

  @Test
  fun testPut_notSavingInPreferences_shouldThrowError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mockk()

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()
    every { secretKeyProvider.encrypt(any()) } returns value.toByteArray()
    every { editor.putString(eq(generateKeyDigest(key)), any()) } returns editor
    every { editor.commit() } returns false

    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(instanceOf<Throwable>(IllegalStateException::class.java))
    encryptedPreferences.put(key, value)
  }

  @Test
  fun testGet_withValueForKey_shouldReturnValue() {
    val key = "key"
    val originalValue = "value"
    val rawValue = originalValue.toByteArray()

    every { preferences.getString(generateKeyDigest(key), null) } returns
      Base64.encodeToString(rawValue, Base64.DEFAULT)

    every { secretKeyProvider.decrypt(rawValue) } returns rawValue
    every { serializer.fromByteArray(rawValue, String::class) } returns originalValue

    val value = encryptedPreferences.get(key, String::class)

    assertEquals(originalValue, value)
  }

  @Test
  fun testGet_withNoValueForKey_shouldThrowException() {
    val key = "key"
    every { preferences.getString(generateKeyDigest(key), null) } returns null

    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(
      instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    encryptedPreferences.get(key, String::class)
  }

  @Test
  fun testGet_withInvalidValueForKey_shouldThrowException() {
    val key = "key"
    val rawValue = "abc".toByteArray()

    every { preferences.getString(generateKeyDigest(key), null) } returns Base64.encodeToString(rawValue, Base64.DEFAULT)
    every { secretKeyProvider.decrypt(any()) } returns rawValue
    every { serializer.fromByteArray(rawValue, Int::class) } returns null

    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(
      instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    encryptedPreferences.get(key, Int::class)
  }

  @Test
  fun testGetAll_withValuesForClass_shouldReturnValuesForClass() {
    val key1 = "key1"
    val originalValue1 = "value1"
    val rawValue1 = getSerializedValue(originalValue1, String::class)

    val key2 = "key2"
    val originalValue2 = 5
    val rawValue2 = getSerializedValue(originalValue2, String::class)

    val key3 = "key3"
    val originalValue3 = "value3"
    val rawValue3 = getSerializedValue(originalValue3, String::class)

    val entries = mapOf(
      key1 to Base64.encodeToString(rawValue1, Base64.DEFAULT),
      key2 to Base64.encodeToString(rawValue2, Base64.DEFAULT),
      key3 to Base64.encodeToString(rawValue3, Base64.DEFAULT)
    )

    every { preferences.all } returns entries.toMutableMap()
    entries.forEach { (key, value) ->
      every { preferences.getString(key, null) } returns value
    }

    every { secretKeyProvider.decrypt(rawValue1) } returns rawValue1
    every { secretKeyProvider.decrypt(rawValue2) } returns rawValue2
    every { secretKeyProvider.decrypt(rawValue3) } returns rawValue3

    val values = encryptedPreferences.getAll(String::class)

    assertEquals(2, values.size)
    assertTrue(values.contains(originalValue1))
    assertTrue(values.contains(originalValue3))
  }

  @Test
  fun testContains_withKey_shouldCallPreferences() {
    val key = "key"

    every { preferences.contains(generateKeyDigest(key)) } returns true
    val isContained = encryptedPreferences.contains(key)

    verify { preferences.contains(generateKeyDigest(key)) }
    assertTrue(isContained)
  }

  @Test
  fun testRemove_withKey_shouldCallEditor() {
    val key = "key"
    val editor: Editor = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { editor.remove(any()) } returns editor

    encryptedPreferences.remove(key)

    verify { editor.remove(generateKeyDigest(key)) }
    verify { editor.apply() }
  }

  @Test
  fun testClear_shouldCallEditor() {
    val editor: Editor = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { editor.clear() } returns editor

    encryptedPreferences.clear()

    verify { editor.clear() }
    verify { editor.apply() }
  }

  private fun <T : Any> getSerializedValue(
    originalValue: Any,
    kClass: KClass<T>
  ): ByteArray {
    val value = if (kClass.isAssignableFrom(originalValue::class)) originalValue as? T else null
    every {
      serializer.fromByteArray(
        originalValue.toString()
          .toByteArray(),
        kClass
      )
    }.returns(value)
    return originalValue.toString()
      .toByteArray()
  }
}
