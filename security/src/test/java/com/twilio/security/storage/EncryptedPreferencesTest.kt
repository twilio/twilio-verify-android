/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Base64
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.storage.key.SecretKeyProvider
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

  private val preferences: SharedPreferences = mock()
  private val secretKeyProvider: SecretKeyProvider = mock()
  private val serializer: Serializer = mock()
  private lateinit var encryptedPreferences: EncryptedPreferences

  @Before
  fun setup() {
    encryptedPreferences = EncryptedPreferences(secretKeyProvider, preferences, serializer)
  }

  @Test
  fun testPut_withValue_shouldSaveEncryptedValue() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    whenever(secretKeyProvider.encrypt(any())).thenReturn(value.toByteArray())
    whenever(editor.putString(eq(generateKeyDigest(key)), any())).thenReturn(editor)
    whenever(editor.commit()).thenReturn(true)
    encryptedPreferences.put(key, value)
    verify(editor).putString(
      generateKeyDigest(key), Base64.encodeToString(value.toByteArray(), Base64.DEFAULT)
    )
    verify(editor).commit()
  }

  @Test
  fun testPut_withError_shouldThrowError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    val exception: RuntimeException = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    whenever(secretKeyProvider.encrypt(any())).thenThrow(exception)
    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(
      instanceOf(
        RuntimeException::class.java
      )
    )
    encryptedPreferences.put(key, value)
  }

  @Test
  fun testPut_notSavingInPreferences_shouldThrowError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    whenever(secretKeyProvider.encrypt(any())).thenReturn(value.toByteArray())
    whenever(editor.putString(eq(generateKeyDigest(key)), any())).thenReturn(editor)
    whenever(editor.commit()).thenReturn(false)
    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(instanceOf(IllegalStateException::class.java))
    encryptedPreferences.put(key, value)
  }

  @Test
  fun testGet_withValueForKey_shouldReturnValue() {
    val key = "key"
    val originalValue = "value"
    val rawValue = originalValue.toByteArray()
    whenever(preferences.getString(generateKeyDigest(key), null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    whenever(secretKeyProvider.decrypt(rawValue)).thenReturn(rawValue)
    whenever(serializer.fromByteArray(rawValue, String::class)).thenReturn(originalValue)
    val value = encryptedPreferences.get(key, String::class)
    assertEquals(originalValue, value)
  }

  @Test
  fun testGet_withNoValueForKey_shouldThrowException() {
    val key = "key"
    whenever(preferences.getString(key, null)).thenReturn(null)
    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(
      instanceOf(
        IllegalArgumentException::class.java
      )
    )
    encryptedPreferences.get(key, String::class)
  }

  @Test
  fun testGet_withInvalidValueForKey_shouldThrowException() {
    val key = "key"
    val rawValue = "abc".toByteArray()
    whenever(preferences.getString(key, null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    whenever(secretKeyProvider.decrypt(any())).thenReturn(rawValue)
    whenever(serializer.fromByteArray(rawValue, Int::class)).thenReturn(null)
    exceptionRule.expect(StorageException::class.java)
    exceptionRule.expectCause(
      instanceOf(
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

    val entries = mapOf<String, String>(
      key1 to Base64.encodeToString(rawValue1, Base64.DEFAULT),
      key2 to Base64.encodeToString(rawValue2, Base64.DEFAULT),
      key3 to Base64.encodeToString(rawValue3, Base64.DEFAULT)
    )
    entries.forEach { (key, value) ->
      whenever(preferences.getString(key, null)).thenReturn(value)
    }
    whenever(secretKeyProvider.decrypt(rawValue1)).thenReturn(rawValue1)
    whenever(secretKeyProvider.decrypt(rawValue2)).thenReturn(rawValue2)
    whenever(secretKeyProvider.decrypt(rawValue3)).thenReturn(rawValue3)
    whenever(preferences.all).thenReturn(entries.toMutableMap())
    val values = encryptedPreferences.getAll(String::class)
    assertEquals(2, values.size)
    assertTrue(values.contains(originalValue1))
    assertTrue(values.contains(originalValue3))
  }

  @Test
  fun testContains_withKey_shouldCallPreferences() {
    val key = "key"
    encryptedPreferences.contains(key)
    verify(preferences).contains(generateKeyDigest(key))
  }

  @Test
  fun testRemove_withKey_shouldCallEditor() {
    val key = "key"
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(editor.remove(any())).thenReturn(editor)
    encryptedPreferences.remove(key)
    verify(editor).remove(generateKeyDigest(key))
    verify(editor).apply()
  }

  @Test
  fun testClear_shouldCallEditor() {
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(editor.clear()).thenReturn(editor)
    encryptedPreferences.clear()
    verify(editor).clear()
    verify(editor).apply()
  }

  private fun <T : Any> getSerializedValue(
    originalValue: Any,
    kClass: KClass<T>
  ): ByteArray {
    val value = if (kClass.isAssignableFrom(originalValue::class)) originalValue as? T else null
    whenever(
      serializer.fromByteArray(
        originalValue.toString()
          .toByteArray(),
        kClass
      )
    ).thenReturn(value)
    return originalValue.toString()
      .toByteArray()
  }
}
