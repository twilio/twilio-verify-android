/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encrypter.AESEncrypter
import com.twilio.security.crypto.key.encrypter.EncryptedData
import com.twilio.security.crypto.key.template.AESGCMNoPaddingEncrypterTemplate
import com.twilio.security.crypto.key.template.EncrypterTemplate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AESGCMNoPaddingEncrypterTests {

  private val keyStore = KeyStore.getInstance(providerName)
      .apply { load(null) }
  private val androidKeyManager = keyManager()
  private lateinit var alias: String

  @Before
  fun setup() {
    alias = System.currentTimeMillis()
        .toString()
    if (keyStore.containsAlias(alias)) {
      keyStore.deleteEntry(alias)
    }
  }

  @After
  fun tearDown() {
    if (this::alias.isInitialized) {
      keyStore.deleteEntry(alias)
    }
  }

  @Test
  fun testEncrypter_withNonExistingKey_shouldReturnEncrypterForNewKey() {
    val template = AESGCMNoPaddingEncrypterTemplate(alias)
    val encrypter = androidKeyManager.encrypter(template)
    assertTrue(encrypter is AESEncrypter)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((encrypter as? AESEncrypter)?.entry)
    assertEquals(
        (keyStore.getEntry(alias, null) as? SecretKeyEntry)?.secretKey,
        (encrypter as AESEncrypter).entry.secretKey
    )
  }

  @Test
  fun testEncrypter_withExistingKey_shouldReturnEncrypterForKey() {
    val template = AESGCMNoPaddingEncrypterTemplate(alias)
    val key = createKey(template)
    val encrypter = androidKeyManager.encrypter(template)
    assertTrue(encrypter is AESEncrypter)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((encrypter as? AESEncrypter)?.entry)
    assertEquals(key, (encrypter as AESEncrypter).entry.secretKey)
  }

  @Test
  fun testEncrypt_withEncrypter_shouldReturnEncryptedData() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingEncrypterTemplate(alias)
    val key = createKey(template)
    val encrypter = androidKeyManager.encrypter(template)
    val encryptedData = encrypter.encrypt(data)
    assertTrue(encryptedData.algorithmParameterSpec is GCMParameterSpec)
    val decrypted = Cipher.getInstance(template.cipherAlgorithm)
        .run {
          init(Cipher.DECRYPT_MODE, key, encryptedData.algorithmParameterSpec)
          doFinal(encryptedData.encrypted)
        }
    assertTrue(data.contentEquals(decrypted))
  }

  @Test
  fun testDecrypt_withEncrypter_shouldReturnData() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingEncrypterTemplate(alias)
    val key = createKey(template)
    val encryptedData = Cipher.getInstance(template.cipherAlgorithm)
        .run {
          init(Cipher.ENCRYPT_MODE, key)
          EncryptedData(parameters.getParameterSpec(template.parameterSpecClass), doFinal(data))
        }
    val encrypter = androidKeyManager.encrypter(template)
    val decrypted = encrypter.decrypt(encryptedData)
    assertTrue(data.contentEquals(decrypted))
  }

  private fun createKey(template: EncrypterTemplate): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
        template.algorithm, providerName
    )
    keyGenerator.init(template.keyGenParameterSpec)
    return keyGenerator.generateKey()
  }
}