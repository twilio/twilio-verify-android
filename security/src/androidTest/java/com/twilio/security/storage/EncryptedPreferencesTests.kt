/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.twilio.security.crypto.key.template.AESGCMNoPaddingEncrypterTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.security.crypto.providerName
import com.twilio.security.storage.key.SecretKeyEncrypter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.Serializable
import java.security.KeyStore

class EncryptedPreferencesTests {

  private val sharedPreferencesName = "TestEncryptedPreferences"
  private val keyStore = KeyStore.getInstance(providerName)
      .apply { load(null) }
  private val androidKeyManager = keyManager()
  private lateinit var alias: String
  private lateinit var encryptedPreferences: EncryptedPreferences
  private lateinit var context: Context
  private lateinit var sharedPreferences: SharedPreferences

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
    val secretKeyEncrypter =
      SecretKeyEncrypter(
          AESGCMNoPaddingEncrypterTemplate(alias, androidKeyManager.contains(alias)),
          androidKeyManager
      )
    secretKeyEncrypter.create()
    encryptedPreferences = EncryptedPreferences(secretKeyEncrypter, sharedPreferences)
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
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(key))
    val value = encryptedPreferences.get(key, Int::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_booleanValue_shouldGetBooleanValue() {
    val key = "value"
    val expectedValue = true
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(key))
    val value = encryptedPreferences.get(key, Boolean::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_stringValue_shouldGetStringValue() {
    val key = "value"
    val expectedValue = "sfdsfdgdfguqweuwr"
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(key))
    val value = encryptedPreferences.get(key, String::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_doubleValue_shouldGetDoubleValue() {
    val key = "value"
    val expectedValue = 1.45657
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(key))
    val value = encryptedPreferences.get(key, Double::class)
    assertEquals(expectedValue, value, 0.0)
  }

  @Test
  fun testPutAndGet_objectValue_shouldGetObjectValue() {
    data class TestObject(
      val name: String,
      val age: Int
    ) : Serializable

    val key = "value"
    val expectedValue = TestObject("name", 33)
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(key))
    val value = encryptedPreferences.get(key, TestObject::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_multipleValues_shouldGetValues() {
    data class TestObject(
      val name: String,
      val age: Int
    ) : Serializable

    val expectedValues = arrayListOf(TestObject("name", 33), true, 1, 1.23434, "test")
    expectedValues.forEachIndexed { index, expectedValue ->
      val key = "key$index"
      encryptedPreferences.put(key, expectedValue)
      assertTrue(sharedPreferences.contains(key))
      val value = encryptedPreferences.get(key, Serializable::class)
      assertEquals(expectedValue, value)
    }
  }

  @Test
  fun testPutAndGetAll_multipleValues_shouldGetValues() {
    data class TestObject(
      val name: String,
      val age: Int
    ) : Serializable

    val expectedValues = arrayListOf(TestObject("name", 33), true, 1, 1.23434, "test")
    expectedValues.forEachIndexed { index, expectedValue ->
      val key = "key$index"
      encryptedPreferences.put(key, expectedValue)
      assertTrue(sharedPreferences.contains(key))
    }
    val values = encryptedPreferences.getAll(Serializable::class)
    values.forEach {
      val index = it.key.replace("key", "")
          .toInt()
      assertEquals(expectedValues[index], it.value)
    }
  }
}