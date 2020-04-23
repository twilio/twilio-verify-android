/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage.key

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.encrypter.AlgorithmParametersSpec
import com.twilio.security.crypto.key.encrypter.EncryptedData
import com.twilio.security.crypto.key.encrypter.Encrypter
import com.twilio.security.crypto.key.template.EncrypterTemplate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.random.Random.Default.nextBytes

class SecretKeyEncrypterTest {

  private val keyManager: KeyManager = mock()
  private val template: EncrypterTemplate = mock()
  private lateinit var secretKeyEncrypter: SecretKeyEncrypter

  @Before
  fun setup() {
    secretKeyEncrypter = SecretKeyEncrypter(template, keyManager)
  }

  @Test
  fun testCreate_shouldCallKeyManager() {
    secretKeyEncrypter.create()
    verify(keyManager).encrypter(template.templateForCreation())
  }

  @Test
  fun testEncrypt_withData_shouldReturnEncrypted() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val encrypter: Encrypter = mock()
    val expectedEncryptedData = EncryptedData(AlgorithmParametersSpec(data, "test", "test"), data)
    whenever(keyManager.encrypter(template)).thenReturn(encrypter)
    whenever(encrypter.encrypt(data)).thenReturn(expectedEncryptedData)
    val encrypted = secretKeyEncrypter.encrypt(data)
    val inputStream = ByteArrayInputStream(encrypted)
    val objectInputStream = ObjectInputStream(inputStream)
    val encryptedData = objectInputStream.readObject() as? EncryptedData
    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test(expected = KeyException::class)
  fun testEncrypt_withErrorEncrypting_shouldThrowError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val encrypter: Encrypter = mock()
    val exception: KeyException = mock()
    whenever(keyManager.encrypter(template)).thenReturn(encrypter)
    whenever(encrypter.encrypt(data)).thenThrow(exception)
    secretKeyEncrypter.encrypt(data)
  }

  @Test
  fun testDecrypt_withData_shouldReturnDecrypted() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val outputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(outputStream)
    objectOutputStream.writeObject(encryptedData)
    val serializedEncryptedData = outputStream.toByteArray()
    val encrypter: Encrypter = mock()
    whenever(keyManager.encrypter(template)).thenReturn(encrypter)
    whenever(encrypter.decrypt(encryptedData)).thenReturn(expectedData)
    val data = secretKeyEncrypter.decrypt(serializedEncryptedData)
    assertEquals(expectedData, data)
  }

  @Test(expected = KeyException::class)
  fun testDecrypt_withErrorEncrypting_shouldThrowError() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val outputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(outputStream)
    objectOutputStream.writeObject(encryptedData)
    val serializedEncryptedData = outputStream.toByteArray()
    val encrypter: Encrypter = mock()
    val exception: KeyException = mock()
    whenever(keyManager.encrypter(template)).thenReturn(encrypter)
    whenever(encrypter.decrypt(encryptedData)).thenThrow(exception)
    secretKeyEncrypter.decrypt(serializedEncryptedData)
  }

  @Test
  fun testDelete_shouldCallKeyManager() {
    val alias = "alias"
    whenever(template.alias).thenReturn(alias)
    secretKeyEncrypter.delete()
    verify(keyManager).delete(alias)
  }
}