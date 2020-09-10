/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.keyManager
import com.twilio.security.crypto.providerName
import com.twilio.security.storage.key.SecretKeyCipher
import java.security.KeyStore
import kotlin.reflect.KClass
import org.json.JSONException
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
    val secretKeyCipher =
      SecretKeyCipher(
        AESGCMNoPaddingCipherTemplate(alias, androidKeyManager.contains(alias)),
        androidKeyManager
      )
    secretKeyCipher.create()
    encryptedPreferences = EncryptedPreferences(
      secretKeyCipher, sharedPreferences,
      TestObjectSerializer(
        DefaultSerializer()
      )
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
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = encryptedPreferences.get(key, Int::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_booleanValue_shouldGetBooleanValue() {
    val key = "value"
    val expectedValue = true
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = encryptedPreferences.get(key, Boolean::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_stringValue_shouldGetStringValue() {
    val key = "value"
    val expectedValue = "sfdsfdgdfguqweuwr"
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = encryptedPreferences.get(key, String::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_doubleValue_shouldGetDoubleValue() {
    val key = "value"
    val expectedValue = 1.45657
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = encryptedPreferences.get(key, Double::class)
    assertEquals(expectedValue, value, 0.0)
  }

  @Test
  fun testPutAndGet_objectValue_shouldGetObjectValue() {
    val key = "value"
    val expectedValue = TestObject("name", 33)
    encryptedPreferences.put(key, expectedValue)
    assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    val value = encryptedPreferences.get(key, TestObject::class)
    assertEquals(expectedValue, value)
  }

  @Test
  fun testPutAndGet_multipleValues_shouldGetValues() {
    val expectedValues = arrayListOf(TestObject("name", 33), true, 1, 1.23434, "test")
    expectedValues.forEachIndexed { index, expectedValue ->
      val key = "key$index"
      encryptedPreferences.put(key, expectedValue)
      assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
      val value = encryptedPreferences.get(key, expectedValue::class)
      assertEquals(expectedValue, value)
    }
  }

  @Test
  fun testPutAndGetAll_multipleValues_shouldGetValues() {
    val expectedValues =
      arrayListOf(TestObject("name", 33), true, 1, TestObject("name1", 346), 1.23434, "test")
    expectedValues.forEachIndexed { index, expectedValue ->
      val key = "key$index"
      encryptedPreferences.put(key, expectedValue)
      assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    }
    val values = encryptedPreferences.getAll(TestObject::class)
    expectedValues.forEach {
      if (it is TestObject) {
        assertTrue(values.contains(it))
      }
    }
  }

  @Test
  fun testPutAndGetAll_multipleIntValues_shouldGetValues() {
    val expectedValues =
      arrayListOf(20, true, 15, TestObject("name1", 346), 1.23434, "1")
    expectedValues.forEachIndexed { index, expectedValue ->
      val key = "key$index"
      encryptedPreferences.put(key, expectedValue)
      assertTrue(sharedPreferences.contains(generateKeyDigest(key)))
    }
    val values = encryptedPreferences.getAll(Int::class)
    expectedValues.forEach {
      if (it is Int) {
        assertTrue(values.contains(it))
      }
    }
  }
}

data class TestObject(
  val name: String,
  val age: Int
)

internal class TestObjectSerializer(private val defaultSerializer: DefaultSerializer) :
  Serializer by defaultSerializer {
  override fun <T : Any> toByteArray(value: T): ByteArray {
    return when (value) {
      is TestObject -> testObjectToByteArray(value)
      else -> defaultSerializer.toByteArray(value)
    }
  }

  override fun <T : Any> fromByteArray(
    data: ByteArray,
    kClass: KClass<T>
  ): T? {
    return when {
      kClass.isAssignableFrom(TestObject::class) -> testObjectFromString(String(data)) as? T
      else -> defaultSerializer.fromByteArray(data, kClass)
    }
  }

  private fun testObjectFromString(string: String): TestObject? = try {
    JSONObject(string)
  } catch (e: JSONException) {
    null
  }?.let {
    TestObject(it.getString("name"), it.getInt("age"))
  }

  private fun testObjectToByteArray(testObject: TestObject): ByteArray = JSONObject().apply {
    put("name", testObject.name)
    put("age", testObject.age)
  }
    .toString()
    .toByteArray()
}
