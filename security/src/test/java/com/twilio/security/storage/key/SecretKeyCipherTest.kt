/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage.key

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.cipher.AlgorithmParametersSpec
import com.twilio.security.crypto.key.cipher.EncryptedData
import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.cipher.fromByteArray
import com.twilio.security.crypto.key.cipher.toByteArray
import com.twilio.security.crypto.key.template.CipherTemplate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random.Default.nextBytes

@RunWith(RobolectricTestRunner::class)
class SecretKeyCipherTest {

  private val keyManager: KeyManager = mock()
  private val template: CipherTemplate = mock()
  private lateinit var secretKeyCipher: SecretKeyCipher

  @Before
  fun setup() {
    secretKeyCipher = SecretKeyCipher(template, keyManager)
  }

  @Test
  fun testCreate_shouldCallKeyManager() {
    secretKeyCipher.create()
    verify(keyManager).cipher(template.templateForCreation())
  }

  @Test
  fun testEncrypt_withData_shouldReturnEncrypted() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mock()
    val expectedEncryptedData = EncryptedData(AlgorithmParametersSpec(data, "test", "test"), data)
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    whenever(cipher.encrypt(data)).thenReturn(expectedEncryptedData)
    val encrypted = secretKeyCipher.encrypt(data)
    val encryptedData = fromByteArray(encrypted)
    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test(expected = KeyException::class)
  fun testEncrypt_withErrorEncrypting_shouldThrowError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mock()
    val exception: KeyException = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    whenever(cipher.encrypt(data)).thenThrow(exception)
    secretKeyCipher.encrypt(data)
  }

  @Test
  fun testDecrypt_withData_shouldReturnDecrypted() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    whenever(cipher.decrypt(encryptedData)).thenReturn(expectedData)
    val data = secretKeyCipher.decrypt(serializedEncryptedData)
    assertEquals(expectedData, data)
  }

  @Test(expected = KeyException::class)
  fun testDecrypt_withErrorEncrypting_shouldThrowError() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mock()
    val exception: KeyException = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    whenever(cipher.decrypt(encryptedData)).thenThrow(exception)
    secretKeyCipher.decrypt(serializedEncryptedData)
  }

  @Test
  fun testDelete_shouldCallKeyManager() {
    val alias = "alias"
    whenever(template.alias).thenReturn(alias)
    secretKeyCipher.delete()
    verify(keyManager).delete(alias)
  }
}