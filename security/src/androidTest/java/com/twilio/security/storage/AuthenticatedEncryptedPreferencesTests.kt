/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.twilio.security.crypto.key.authentication.BiometricAuthenticator
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.security.crypto.providerName
import com.twilio.security.storage.key.BiometricSecretKey
import java.security.KeyStore
import java.security.Signature
import javax.crypto.Cipher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class AuthenticatedEncryptedPreferencesTests {

  private val sharedPreferencesName = "TestEncryptedPreferences"
  private val keyStore = KeyStore.getInstance(providerName)
    .apply { load(null) }
  private val androidKeyManager = keyManager()
  private lateinit var alias: String
  private lateinit var authenticatedEncryptedPreferences: AuthenticatedEncryptedPreferences
  private lateinit var context: Context
  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var authenticator: BiometricAuthenticator

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    alias = System.currentTimeMillis()
      .toString()
    if (keyStore.containsAlias(alias)) {
      keyStore.deleteEntry(alias)
    }
    sharedPreferences =
      context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    val biometricSecretKey =
      BiometricSecretKey(
        AESGCMNoPaddingCipherTemplate(alias, androidKeyManager.contains(alias)),
        androidKeyManager
      )
    biometricSecretKey.create()
    authenticator = TestAuthenticator()
    authenticatedEncryptedPreferences = AuthenticatedEncryptedPreferences(
      biometricSecretKey, sharedPreferences, TestObjectSerializer(DefaultSerializer())
    )
  }

  @After
  fun tearDown() {
    if (this::alias.isInitialized) {
      keyStore.deleteEntry(alias)
    }
    sharedPreferences.edit()
      .clear()
      .apply()
  }

  @Test
  fun testPutAndGet_intValue_shouldGetIntValue() {
    val key = "value"
    val expectedValue = 123
    authenticatedEncryptedPreferences.put(
      key, expectedValue, authenticator, {},
      {
        fail(it.message)
      }
    )
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = authenticatedEncryptedPreferences.get(
      key, Int::class, authenticator,
      {
        assertEquals(expectedValue, it)
      },
      {
        fail(it.message)
      }
    )
  }

  @Test
  fun testPutAndGet_booleanValue_shouldGetBooleanValue() {
    val key = "value"
    val expectedValue = true
    authenticatedEncryptedPreferences.put(
      key, expectedValue, authenticator, {},
      {
        fail(it.message)
      }
    )
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = authenticatedEncryptedPreferences.get(
      key, Boolean::class, authenticator,
      {
        assertEquals(expectedValue, it)
      },
      {
        fail(it.message)
      }
    )
  }

  @Test
  fun testPutAndGet_stringValue_shouldGetStringValue() {
    val key = "value"
    val expectedValue = "sfdsfdgdfguqweuwr"
    authenticatedEncryptedPreferences.put(
      key, expectedValue, authenticator, {},
      {
        fail(it.message)
      }
    )
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = authenticatedEncryptedPreferences.get(
      key, String::class, authenticator,
      {
        assertEquals(expectedValue, it)
      },
      {
        fail(it.message)
      }
    )
  }

  @Test
  fun testPutAndGet_doubleValue_shouldGetDoubleValue() {
    val key = "value"
    val expectedValue = 1.45657
    authenticatedEncryptedPreferences.put(
      key, expectedValue, authenticator, {},
      {
        fail(it.message)
      }
    )
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = authenticatedEncryptedPreferences.get(
      key, Double::class, authenticator,
      {
        assertEquals(expectedValue, it, 0.0)
      },
      {
        fail(it.message)
      }
    )
  }

  @Test
  fun testPutAndGet_objectValue_shouldGetObjectValue() {
    val key = "value"
    val expectedValue = TestObject("name", 33)
    authenticatedEncryptedPreferences.put(
      key, expectedValue, authenticator, {},
      {
        fail(it.message)
      }
    )
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = authenticatedEncryptedPreferences.get(
      key, TestObject::class, authenticator,
      {
        assertEquals(expectedValue, it)
      },
      {
        fail(it.message)
      }
    )
  }

  @Test
  fun testPutAndGet_multipleValues_shouldGetValues() {
    val expectedValues = arrayListOf(TestObject("name", 33), true, 1, 1.23434, "test")
    expectedValues.forEachIndexed { index, expectedValue ->
      val key = "key$index"
      authenticatedEncryptedPreferences.put(
        key, expectedValue, authenticator, {},
        {
          fail(it.message)
        }
      )
      assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
      val value = authenticatedEncryptedPreferences.get(
        key, expectedValue::class, authenticator,
        {
          assertEquals(expectedValue, it)
        },
        {
          fail(it.message)
        }
      )
    }
  }
}

internal class TestAuthenticator : BiometricAuthenticator {
  val exception: Exception? = null

  override fun checkAvailability() {
    exception?.let { throw exception }
  }

  override fun startAuthentication(signatureObject: Signature, success: (Signature) -> Unit, error: (Exception) -> Unit) {
    exception?.let { error(exception) } ?: success(signatureObject)
  }

  override fun startAuthentication(cipherObject: Cipher, success: (Cipher) -> Unit, error: (Exception) -> Unit) {
    exception?.let { error(exception) } ?: success(cipherObject)
  }
}
