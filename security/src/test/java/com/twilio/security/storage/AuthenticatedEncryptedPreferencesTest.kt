/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Base64
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.authentication.BiometricAuthenticator
import com.twilio.security.crypto.key.authentication.BiometricError.NoBiometricEnrolled
import com.twilio.security.crypto.key.authentication.BiometricException
import com.twilio.security.storage.key.BiometricSecretKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticatedEncryptedPreferencesTest {

  private val preferences: SharedPreferences = mockk(relaxed = true)
  private val storageAlias: String = "alias"
  private val keyManager: KeyManager = mockk(relaxed = true)
  private val serializer: Serializer = mockk(relaxed = true)
  private val authenticator: BiometricAuthenticator = mockk(relaxed = true)
  private lateinit var encryptedPreferences: AuthenticatedEncryptedPreferences
  private var biometricSecretKey: BiometricSecretKey = mockk(relaxed = true)

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
    val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()

    val encryptedCallback = slot<(ByteArray) -> Unit>()
    every { biometricSecretKey.encrypt(any(), eq(authenticator), capture(encryptedCallback), any()) } answers {
      encryptedCallback.captured.invoke(value.toByteArray())
    }

    every { editor.putString(eq(generateKeyDigest(key)), any()) } returns editor
    every { editor.commit() } returns true

    encryptedPreferences.put(
      key, value, authenticator, {},
      { fail("Encryption should not fail") }
    )

    verify { editor.putString(generateKeyDigest(key), Base64.encodeToString(value.toByteArray(), Base64.DEFAULT)) }
    verify { editor.commit() }
  }

  @Test
  fun testPut_withError_shouldCallError() {
    val key = "key"
    val value = "value"
    val editor = mockk<SharedPreferences.Editor>()
    val exception = mockk<RuntimeException>(relaxed = true)

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()

    val errorCallback = slot<(Exception) -> Unit>()
    every { biometricSecretKey.encrypt(any(), eq(authenticator), any(), capture(errorCallback)) } answers {
      errorCallback.captured.invoke(exception)
    }

    encryptedPreferences.put(
      key, value, authenticator,
      { fail("Encryption should not succeed") },
      { error ->
        assertTrue(error is StorageException)
        assertEquals(exception, error.cause)
      }
    )
  }

  @Test
  fun testPut_withBiometricError_shouldCallError() {
    val key = "key"
    val value = "value"
    val editor = mockk<SharedPreferences.Editor>()
    val exception = BiometricException(NoBiometricEnrolled)

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()

    val errorCallback = slot<(Exception) -> Unit>()
    every { biometricSecretKey.encrypt(any(), eq(authenticator), any(), capture(errorCallback)) } answers {
      errorCallback.captured.invoke(exception)
    }

    encryptedPreferences.put(
      key, value, authenticator,
      { fail("Encryption should not succeed") },
      { error ->
        assertTrue(error is StorageException)
        assertEquals(exception, error.cause)
        assertEquals(NoBiometricEnrolled.message, exception.message)
      }
    )
  }

  @Test
  fun testPut_notSavingInPreferences_shouldCallError() {
    val key = "key"
    val value = "value"
    val editor = mockk<SharedPreferences.Editor>()

    every { preferences.edit() } returns editor
    every { serializer.toByteArray(value) } returns value.toByteArray()

    val encryptionCallback = slot<(ByteArray) -> Unit>()
    every { biometricSecretKey.encrypt(any(), eq(authenticator), capture(encryptionCallback), any()) } answers {
      encryptionCallback.captured.invoke(value.toByteArray())
    }

    every { editor.putString(eq(generateKeyDigest(key)), any()) } returns editor
    every { editor.commit() } returns false

    encryptedPreferences.put(
      key, value, authenticator,
      { fail("Encryption should not succeed") },
      { error ->
        assertTrue(error is StorageException)
        assertTrue(error.cause is IllegalStateException)
      }
    )
  }

  @Test
  fun testPut_withKeyManagerNotContainingStorageAndPreferencesWithoutData_shouldCallCreate() {
    every { keyManager.contains(storageAlias) } returns false
    every { preferences.all } returns emptyMap()

    encryptedPreferences.put("key", "value", authenticator, {}, {})

    verify { biometricSecretKey.create() }
  }

  @Test
  fun testPut_withKeyManagerContainingStorageAndPreferencesWithoutData_shouldNotCallCreate() {
    every { keyManager.contains(storageAlias) } returns true
    every { preferences.all } returns emptyMap()

    encryptedPreferences.put("key", "value", authenticator, {}, {})

    verify(exactly = 0) { biometricSecretKey.create() }
  }

  @Test
  fun testPut_withKeyManagerNotContainingStorageAndPreferencesWithData_shouldNotCallCreate() {
    every { keyManager.contains(storageAlias) } returns false
    every { preferences.all } returns mapOf("key" to "value")

    encryptedPreferences.put("key", "value", authenticator, {}, {})

    verify(exactly = 0) { biometricSecretKey.create() }
  }

  @Test
  fun testGet_withValueForKey_shouldReturnValue() {
    val key = "key"
    val originalValue = "value"
    val rawValue = originalValue.toByteArray()

    every { preferences.getString(generateKeyDigest(key), null) } returns
      Base64.encodeToString(rawValue, Base64.DEFAULT)

    val decryptCallback = slot<(ByteArray) -> Unit>()
    every { biometricSecretKey.decrypt(eq(rawValue), eq(authenticator), capture(decryptCallback), any()) } answers {
      decryptCallback.captured.invoke(rawValue)
    }

    every { serializer.fromByteArray(rawValue, String::class) } returns originalValue

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

    every { preferences.getString(key, null) } returns null

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

    every { preferences.getString(key, null) } returns Base64.encodeToString(rawValue, Base64.DEFAULT)

    val decryptCaptor = slot<(ByteArray) -> Unit>()
    every { biometricSecretKey.decrypt(eq(rawValue), eq(authenticator), capture(decryptCaptor), any()) } answers {
      decryptCaptor.captured.invoke(rawValue)
    }

    every { serializer.fromByteArray(rawValue, Int::class) } returns null

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

    every { preferences.getString(generateKeyDigest(key), null) } returns Base64.encodeToString(rawValue, Base64.DEFAULT)

    val errorCaptor = slot<(Exception) -> Unit>()
    every { biometricSecretKey.decrypt(eq(rawValue), eq(authenticator), any(), capture(errorCaptor)) } answers {
      errorCaptor.captured.invoke(exception)
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

    every { preferences.getString(generateKeyDigest(key), null) } returns Base64.encodeToString(rawValue, Base64.DEFAULT)
    every { keyManager.contains(storageAlias) } returns false
    every { preferences.all } returns emptyMap()

    encryptedPreferences.get(key, String::class, authenticator, {}, {})

    verify { biometricSecretKey.create() }
  }

  @Test
  fun testGet_withKeyManagerContainingStorageAndPreferencesWithoutData_shouldNotCallCreate() {
    val key = "key"
    val rawValue = "value".toByteArray()

    every { preferences.getString(generateKeyDigest(key), null) } returns Base64.encodeToString(rawValue, Base64.DEFAULT)
    every { keyManager.contains(storageAlias) } returns true
    every { preferences.all } returns emptyMap()

    encryptedPreferences.get(key, String::class, authenticator, {}, {})

    verify(exactly = 0) { biometricSecretKey.create() }
  }

  @Test
  fun testGet_withKeyManagerNotContainingStorageAndPreferencesWithData_shouldNotCallCreate() {
    val key = "key"
    val rawValue = "value".toByteArray()

    every { preferences.getString(generateKeyDigest(key), null) } returns Base64.encodeToString(rawValue, Base64.DEFAULT)
    every { keyManager.contains(storageAlias) } returns false
    every { preferences.all } returns mapOf("key" to "value")

    encryptedPreferences.get(key, String::class, authenticator, {}, {})

    verify(exactly = 0) { biometricSecretKey.create() }
  }

  @Test
  fun testContains_withKey_shouldCallPreferences() {
    val key = "key"
    encryptedPreferences.contains(key)
    verify { preferences.contains(generateKeyDigest(key)) }
  }

  @Test
  fun testRemove_withKey_shouldCallEditor() {
    val key = "key"
    val editor: SharedPreferences.Editor = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { editor.remove(any()) } returns editor

    encryptedPreferences.remove(key)

    verify { editor.remove(generateKeyDigest(key)) }
    verify { editor.apply() }
  }

  @Test
  fun testClear_shouldCallEditor() {
    val editor: SharedPreferences.Editor = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { editor.clear() } returns editor

    encryptedPreferences.clear()

    verify { editor.clear() }
    verify { editor.apply() }
  }

  @Test
  fun testRecreate_shouldClearPreferences_shouldDeleteKey_shouldCreateANewKey() {
    val editor: SharedPreferences.Editor = mockk(relaxed = true)

    every { preferences.edit() } returns editor
    every { editor.clear() } returns editor
    every { keyManager.contains(storageAlias) } returns true

    encryptedPreferences.recreate()

    verify { editor.clear() }
    verify { editor.apply() }
    verify { biometricSecretKey.delete() }
    verify { biometricSecretKey.create() }
  }
}
