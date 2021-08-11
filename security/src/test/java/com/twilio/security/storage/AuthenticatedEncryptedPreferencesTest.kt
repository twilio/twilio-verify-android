/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Base64
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.authentication.BiometricAuthenticator
import com.twilio.security.crypto.key.authentication.BiometricError.NoBiometricEnrolled
import com.twilio.security.crypto.key.authentication.BiometricException
import com.twilio.security.storage.key.BiometricSecretKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticatedEncryptedPreferencesTest {

  private val preferences: SharedPreferences = mock()
  private val storageAlias: String = "alias"
  private val keyManager: KeyManager = mock()
  private val serializer: Serializer = mock()
  private val authenticator: BiometricAuthenticator = mock()
  private lateinit var encryptedPreferences: AuthenticatedEncryptedPreferences
  private var biometricSecretKey: BiometricSecretKey = mock()

  @Before
  fun setup() {
    encryptedPreferences =
      AuthenticatedEncryptedPreferences(preferences, storageAlias, keyManager, serializer)
    encryptedPreferences.biometricSecretKey = biometricSecretKey
  }

  @Test
  fun testPut_withValue_shouldSaveEncryptedValue() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    argumentCaptor<(ByteArray) -> Unit>().apply {
      whenever(biometricSecretKey.encrypt(any(), eq(authenticator), capture(), any())).then {
        firstValue.invoke(value.toByteArray())
      }
    }
    whenever(editor.putString(eq(generateKeyDigest(key)), any())).thenReturn(editor)
    whenever(editor.commit()).thenReturn(true)

    encryptedPreferences.put(
      key, value, authenticator, {},
      {
        fail()
      }
    )
    verify(editor).putString(
      generateKeyDigest(key), Base64.encodeToString(value.toByteArray(), Base64.DEFAULT)
    )
    verify(editor).commit()
  }

  @Test
  fun testPut_withError_shouldCallError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    val exception: RuntimeException = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(biometricSecretKey.encrypt(any(), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
    }
    encryptedPreferences.put(
      key, value, authenticator, { fail() },
      {
        assertTrue(it is StorageException)
        assertEquals(exception, it.cause)
      }
    )
  }

  @Test
  fun testPut_withBiometricError_shouldCallError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    val exception = BiometricException(NoBiometricEnrolled)
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(biometricSecretKey.encrypt(any(), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
    }
    encryptedPreferences.put(
      key, value, authenticator, { fail() },
      {
        assertTrue(it is StorageException)
        assertEquals(exception, it.cause)
        assertEquals(NoBiometricEnrolled.message, exception.message)
      }
    )
  }

  @Test
  fun testPut_notSavingInPreferences_shouldCallError() {
    val key = "key"
    val value = "value"
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(serializer.toByteArray(value)).thenReturn(value.toByteArray())
    argumentCaptor<(ByteArray) -> Unit>().apply {
      whenever(biometricSecretKey.encrypt(any(), eq(authenticator), capture(), any())).then {
        firstValue.invoke(value.toByteArray())
      }
    }
    whenever(editor.putString(eq(generateKeyDigest(key)), any())).thenReturn(editor)
    whenever(editor.commit()).thenReturn(false)
    encryptedPreferences.put(
      key, value, authenticator, { fail() },
      {
        assertTrue(it is StorageException)
        assertTrue(it.cause is IllegalStateException)
      }
    )
  }

  @Test
  fun testPut_withKeyManagerNotContainingStorageAndPreferencesWithoutData_shouldCallCreate() {
    whenever(keyManager.contains(storageAlias)).thenReturn(false)
    whenever(preferences.all).thenReturn(emptyMap<String, Any>())
    encryptedPreferences.put("key", "value", authenticator, {}, {})
    verify(biometricSecretKey).create()
  }

  @Test
  fun testPut_withKeyManagerContainingStorageAndPreferencesWithoutData_shouldNotCallCreate() {
    whenever(keyManager.contains(storageAlias)).thenReturn(true)
    whenever(preferences.all).thenReturn(emptyMap<String, Any>())
    encryptedPreferences.put("key", "value", authenticator, {}, {})
    verify(biometricSecretKey, never()).create()
  }

  @Test
  fun testPut_withKeyManagerNotContainingStorageAndPreferencesWithData_shouldNotCallCreate() {
    whenever(keyManager.contains(storageAlias)).thenReturn(false)
    whenever(preferences.all).thenReturn(mapOf("key" to "value"))
    encryptedPreferences.put("key", "value", authenticator, {}, {})
    verify(biometricSecretKey, never()).create()
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
    argumentCaptor<(ByteArray) -> Unit>().apply {
      whenever(biometricSecretKey.decrypt(eq(rawValue), eq(authenticator), capture(), any())).then {
        firstValue.invoke(rawValue)
      }
    }
    whenever(serializer.fromByteArray(rawValue, String::class)).thenReturn(originalValue)
    encryptedPreferences.get(
      key,
      String::class,
      authenticator,
      {
        assertEquals(originalValue, it)
      },
      {
        fail()
      }
    )
  }

  @Test
  fun testGet_withNoValueForKey_shouldCallError() {
    val key = "key"
    whenever(preferences.getString(key, null)).thenReturn(null)
    encryptedPreferences.get(
      key, String::class, authenticator, { fail() },
      {
        assertTrue(it is StorageException)
        assertTrue(it.cause is IllegalArgumentException)
      }
    )
  }

  @Test
  fun testGet_withInvalidValueForKey_shouldCallError() {
    val key = "key"
    val rawValue = "abc".toByteArray()
    whenever(preferences.getString(key, null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    argumentCaptor<(ByteArray) -> Unit>().apply {
      whenever(biometricSecretKey.decrypt(eq(rawValue), eq(authenticator), capture(), any())).then {
        firstValue.invoke(rawValue)
      }
    }
    whenever(serializer.fromByteArray(rawValue, Int::class)).thenReturn(null)
    encryptedPreferences.get(
      key, Int::class, authenticator, { fail() },
      {
        assertTrue(it is StorageException)
        assertTrue(it.cause is IllegalArgumentException)
      }
    )
  }

  @Test
  fun testGet_withBiometricError_shouldCallError() {
    val exception = BiometricException(NoBiometricEnrolled)
    val key = "key"
    val originalValue = "value"
    val rawValue = originalValue.toByteArray()
    whenever(preferences.getString(generateKeyDigest(key), null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(biometricSecretKey.decrypt(eq(rawValue), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
    }
    encryptedPreferences.get(
      key,
      String::class,
      authenticator, { fail() },
      {
        assertTrue(it is StorageException)
        assertEquals(exception, it.cause)
        assertEquals(NoBiometricEnrolled.message, exception.message)
      }
    )
  }

  @Test
  fun testGet_withKeyManagerNotContainingStorageAndPreferencesWithoutData_shouldCallCreate() {
    val key = "key"
    val rawValue = "value".toByteArray()
    whenever(preferences.getString(generateKeyDigest(key), null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    whenever(keyManager.contains(storageAlias)).thenReturn(false)
    whenever(preferences.all).thenReturn(emptyMap<String, Any>())
    encryptedPreferences.get(key, String::class, authenticator, {}, {})
    verify(biometricSecretKey).create()
  }

  @Test
  fun testGet_withKeyManagerContainingStorageAndPreferencesWithoutData_shouldNotCallCreate() {
    val key = "key"
    val rawValue = "value".toByteArray()
    whenever(preferences.getString(generateKeyDigest(key), null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    whenever(keyManager.contains(storageAlias)).thenReturn(true)
    whenever(preferences.all).thenReturn(emptyMap<String, Any>())
    encryptedPreferences.get(key, String::class, authenticator, {}, {})
    verify(biometricSecretKey, never()).create()
  }

  @Test
  fun testGet_withKeyManagerNotContainingStorageAndPreferencesWithData_shouldNotCallCreate() {
    val key = "key"
    val rawValue = "value".toByteArray()
    whenever(preferences.getString(generateKeyDigest(key), null)).thenReturn(
      Base64.encodeToString(
        rawValue,
        Base64.DEFAULT
      )
    )
    whenever(keyManager.contains(storageAlias)).thenReturn(false)
    whenever(preferences.all).thenReturn(mapOf("key" to "value"))
    encryptedPreferences.get("key", String::class, authenticator, {}, {})
    verify(biometricSecretKey, never()).create()
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

  @Test
  fun testRecreate_withKeyManagerNotContainingStorageAndPreferencesWithoutData_shouldCallCreate() {
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(editor.clear()).thenReturn(editor)
    whenever(keyManager.contains(storageAlias)).thenReturn(true).thenReturn(false)
    whenever(preferences.all).thenReturn(emptyMap<String, Any>())
    encryptedPreferences.recreate()
    verify(editor).clear()
    verify(editor).apply()
    verify(biometricSecretKey).delete()
    verify(biometricSecretKey).create()
  }

  @Test
  fun testRecreate_withKeyManagerNotContainingStorageAndPreferencesWithData_shouldNotCallCreate() {
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(editor.clear()).thenReturn(editor)
    whenever(keyManager.contains(storageAlias)).thenReturn(false)
    whenever(preferences.all).thenReturn(mapOf("key" to "value"))
    encryptedPreferences.recreate()
    verify(editor).clear()
    verify(editor).apply()
    verify(biometricSecretKey).delete()
    verify(biometricSecretKey, never()).create()
  }

  @Test
  fun testRecreate_withKeyManagerContainingStorageAndPreferencesWithData_shouldNotCallCreate() {
    val editor: Editor = mock()
    val keyManager: KeyManager = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(editor.clear()).thenReturn(editor)
    whenever(keyManager.contains(storageAlias)).thenReturn(true)
    whenever(preferences.all).thenReturn(mapOf("key" to "value"))
    encryptedPreferences.recreate()
    verify(editor).clear()
    verify(editor).apply()
    verify(biometricSecretKey).delete()
    verify(biometricSecretKey, never()).create()
  }

  @Test
  fun testRecreate_withKeyManagerContainingStorageAndPreferencesWithoutData_shouldNotCallCreate() {
    val editor: Editor = mock()
    whenever(preferences.edit()).thenReturn(editor)
    whenever(editor.clear()).thenReturn(editor)
    whenever(keyManager.contains(storageAlias)).thenReturn(true)
    whenever(preferences.all).thenReturn(emptyMap<String, Any>())
    encryptedPreferences.recreate()
    verify(editor).clear()
    verify(editor).apply()
    verify(biometricSecretKey).delete()
    verify(biometricSecretKey, never()).create()
  }
}
