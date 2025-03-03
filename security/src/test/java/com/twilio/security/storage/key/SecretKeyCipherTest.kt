/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage.key

import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.cipher.AlgorithmParametersSpec
import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.cipher.EncryptedData
import com.twilio.security.crypto.key.cipher.fromByteArray
import com.twilio.security.crypto.key.cipher.toByteArray
import com.twilio.security.crypto.key.template.CipherTemplate
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.random.Random.Default.nextBytes
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecretKeyCipherTest {

  private val keyManager: KeyManager = mockk(relaxed = true)
  private val template: CipherTemplate = mockk(relaxed = true)
  private lateinit var secretKeyCipher: SecretKeyCipher

  @Before
  fun setup() {
    secretKeyCipher = SecretKeyCipher(template, keyManager)
  }

  @Test
  fun testCreate_shouldCallKeyManager() {
    secretKeyCipher.create()
    verify { keyManager.cipher(template.templateForCreation()) }
  }

  @Test
  fun testEncrypt_withData_shouldReturnEncrypted() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mockk()
    val expectedEncryptedData = EncryptedData(AlgorithmParametersSpec(data, "test", "test"), data)
    every { keyManager.cipher(template) }.returns(cipher)
    every { cipher.encrypt(data) }.returns(expectedEncryptedData)
    val encrypted = secretKeyCipher.encrypt(data)
    val encryptedData = fromByteArray(encrypted)
    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test(expected = KeyException::class)
  fun testEncrypt_withErrorEncrypting_shouldThrowError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mockk()
    val exception: KeyException = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.encrypt(data) } throws exception

    secretKeyCipher.encrypt(data)
  }

  @Test
  fun testDecrypt_withData_shouldReturnDecrypted() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mockk()

    every { keyManager.cipher(template) }.returns(cipher)
    every { cipher.decrypt(encryptedData) }.returns(expectedData)
    val data = secretKeyCipher.decrypt(serializedEncryptedData)
    assertEquals(expectedData, data)
  }

  @Test(expected = KeyException::class)
  fun testDecrypt_withErrorEncrypting_shouldThrowError() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mockk()
    val exception: KeyException = mockk()
    every { keyManager.cipher(template) }.returns(cipher)
    every { cipher.decrypt(encryptedData) }.throws(exception)
    secretKeyCipher.decrypt(serializedEncryptedData)
  }

  @Test
  fun testDelete_shouldCallKeyManager() {
    val alias = "alias"
    every { template.alias }.returns(alias)
    secretKeyCipher.delete()
    verify { keyManager.delete(alias) }
  }
}
