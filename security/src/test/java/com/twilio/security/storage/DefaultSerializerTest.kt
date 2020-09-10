/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultSerializerTest {

  private lateinit var defaultSerializer: DefaultSerializer

  @Before
  fun setup() {
    defaultSerializer = DefaultSerializer()
  }

  @Test
  fun testToByteArray_withString_shouldReturnByteArray() {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val stringValue = (1..15)
      .map { Random.nextInt(0, charPool.size) }
      .map(charPool::get)
      .joinToString("")
    val expectedByteArray = stringValue.toByteArray()
    val byteArray = defaultSerializer.toByteArray(stringValue)
    assertTrue(expectedByteArray.contentEquals(byteArray))
    assertEquals(stringValue, String(byteArray))
  }

  @Test
  fun testToByteArray_withInt_shouldReturnByteArray() {
    val intValue = Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE)
    val expectedByteArray = intValue.toString()
      .toByteArray()
    val byteArray = defaultSerializer.toByteArray(intValue)
    assertTrue(expectedByteArray.contentEquals(byteArray))
    assertEquals(intValue, String(byteArray).toInt())
  }

  @Test
  fun testToByteArray_withDouble_shouldReturnByteArray() {
    val doubleValue = Random.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE)
    val expectedByteArray = doubleValue.toString()
      .toByteArray()
    val byteArray = defaultSerializer.toByteArray(doubleValue)
    assertTrue(expectedByteArray.contentEquals(byteArray))
    assertEquals(doubleValue, String(byteArray).toDouble(), 0.0)
  }

  @Test
  fun testToByteArray_withBoolean_shouldReturnByteArray() {
    val booleanValue = Random.nextBoolean()
    val expectedByteArray = booleanValue.toString()
      .toByteArray()
    val byteArray = defaultSerializer.toByteArray(booleanValue)
    assertTrue(expectedByteArray.contentEquals(byteArray))
    assertEquals(booleanValue, String(byteArray).toBoolean())
  }

  @Test
  fun testToByteArray_withFloat_shouldReturnByteArray() {
    val floatValue = Random.nextFloat()
    val expectedByteArray = floatValue.toString()
      .toByteArray()
    val byteArray = defaultSerializer.toByteArray(floatValue)
    assertTrue(expectedByteArray.contentEquals(byteArray))
    assertEquals(floatValue, String(byteArray).toFloat())
  }

  @Test
  fun testToByteArray_withLong_shouldReturnByteArray() {
    val longValue = Random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE)
    val expectedByteArray = longValue.toString()
      .toByteArray()
    val byteArray = defaultSerializer.toByteArray(longValue)
    assertTrue(expectedByteArray.contentEquals(byteArray))
    assertEquals(longValue, String(byteArray).toLong())
  }

  @Test
  fun testFromByteArray_withString_shouldReturnValue() {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val stringValue = (1..15)
      .map { Random.nextInt(0, charPool.size) }
      .map(charPool::get)
      .joinToString("")
    val byteArray = stringValue.toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, String::class)
    assertEquals(stringValue, value)
  }

  @Test
  fun testFromByteArray_withInt_shouldReturnValue() {
    val intValue = Random.nextInt(Int.MIN_VALUE, Int.MAX_VALUE)
    val byteArray = intValue.toString()
      .toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, Int::class)
    assertEquals(intValue, value)
  }

  @Test
  fun testFromByteArray_withDouble_shouldReturnValue() {
    val doubleValue = Random.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE)
    val byteArray = doubleValue.toString()
      .toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, Double::class)
    assertEquals(doubleValue, value)
  }

  @Test
  fun testFromByteArray_withBoolean_shouldReturnValue() {
    val booleanValue = Random.nextBoolean()
    val byteArray = booleanValue.toString()
      .toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, Boolean::class)
    assertEquals(booleanValue, value)
  }

  @Test
  fun testFromByteArray_withFloat_shouldReturnValue() {
    val floatValue = Random.nextFloat()
    val byteArray = floatValue.toString()
      .toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, Float::class)
    assertEquals(floatValue, value)
  }

  @Test
  fun testFromByteArray_withLong_shouldReturnValue() {
    val longValue = Random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE)
    val byteArray = longValue.toString()
      .toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, Long::class)
    assertEquals(longValue, value)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testToByteArray_withObject_shouldThrowException() {
    val value: List<String> = arrayListOf("a", "b")
    defaultSerializer.toByteArray(value)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testFromByteArray_withObjectType_shouldThrowException() {
    val longValue = Random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE)
    val byteArray = longValue.toString()
      .toByteArray()
    defaultSerializer.fromByteArray(byteArray, List::class)
  }

  @Test(expected = NumberFormatException::class)
  fun testFromByteArray_withStringAndIntType_shouldThrowException() {
    val stringValue = "abc"
    val byteArray = stringValue.toByteArray()
    defaultSerializer.fromByteArray(byteArray, Int::class)
  }

  @Test
  fun testFromByteArray_withStringAndBooleanType_shouldReturnNull() {
    val stringValue = "abc"
    val byteArray = stringValue.toByteArray()
    val value = defaultSerializer.fromByteArray(byteArray, Boolean::class)
    assertNull(value)
  }
}
