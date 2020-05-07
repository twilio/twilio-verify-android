/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encrypter.AESEncrypter
import com.twilio.security.crypto.key.encrypter.AlgorithmParametersSpec
import com.twilio.security.crypto.key.encrypter.EncryptedData
import com.twilio.security.crypto.key.template.AESGCMNoPaddingEncrypterTemplate
import com.twilio.security.crypto.key.template.EncrypterTemplate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.AlgorithmParameters
import java.security.KeyStore
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
    val template = AESGCMNoPaddingEncrypterTemplate(alias).templateForCreation()
    val encrypter = androidKeyManager.encrypter(template)
    assertTrue(encrypter is AESEncrypter)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((encrypter as? AESEncrypter)?.key)
    assertEquals(keyStore.getKey(alias, null), (encrypter as AESEncrypter).key)
  }

  @Test
  fun testEncrypter_withExistingKey_shouldReturnEncrypterForKey() {
    val template = AESGCMNoPaddingEncrypterTemplate(alias)
    val key = createKey(template)
    val encrypter = androidKeyManager.encrypter(template)
    assertTrue(encrypter is AESEncrypter)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((encrypter as? AESEncrypter)?.key)
    assertEquals(key, (encrypter as AESEncrypter).key)
  }

  @Test
  fun testEncrypt_withEncrypter_shouldReturnEncryptedData() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingEncrypterTemplate(alias).templateForCreation()
    val encrypter = androidKeyManager.encrypter(template)
    val encryptedData = encrypter.encrypt(data)
    val decrypted = encrypter.decrypt(encryptedData)
    assertTrue(data.contentEquals(decrypted))
  }

  @Test
  fun testDecrypt_withEncrypter_shouldReturnData() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingEncrypterTemplate(alias).templateForCreation()
    val key = createKey(template)
    val encryptedData = Cipher.getInstance(template.cipherAlgorithm)
        .run {
          init(Cipher.ENCRYPT_MODE, key)
          EncryptedData(
              AlgorithmParametersSpec(
                  parameters.encoded, parameters.provider.name, parameters.algorithm
              ), doFinal(data)
          )
        }
    val encrypter = androidKeyManager.encrypter(template)
    val decrypted = encrypter.decrypt(encryptedData)
    assertTrue(data.contentEquals(decrypted))
  }

  @Test
  fun testEncrypt_withSameInput_shouldReturnDifferentEncryptedDataForEachEncrypt() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingEncrypterTemplate(alias).templateForCreation()
    val encrypter = androidKeyManager.encrypter(template)
    val encryptedData1 = encrypter.encrypt(data)
    val encryptedData2 = encrypter.encrypt(data)
    assertNotEquals(encryptedData1, encryptedData2)
    val algorithmParametersSpec1 = AlgorithmParameters.getInstance(
        encryptedData1.algorithmParameters.algorithm,
        encryptedData1.algorithmParameters.provider
    )
        .apply {
          init(encryptedData1.algorithmParameters.encoded)
        }.getParameterSpec(GCMParameterSpec::class.java) as GCMParameterSpec
    val algorithmParametersSpec2 = AlgorithmParameters.getInstance(
        encryptedData2.algorithmParameters.algorithm,
        encryptedData2.algorithmParameters.provider
    )
        .apply {
          init(encryptedData2.algorithmParameters.encoded)
        }.getParameterSpec(GCMParameterSpec::class.java) as GCMParameterSpec
    assertFalse(algorithmParametersSpec1.iv!!.contentEquals(algorithmParametersSpec2.iv))
    assertEquals(algorithmParametersSpec1.tLen, algorithmParametersSpec2.tLen)
  }

  @Test
  fun testDelete_withExistingKeyPair_shouldDeleteAlias() {
    val template = AESGCMNoPaddingEncrypterTemplate(alias).templateForCreation()
    createKey(template)
    assertTrue(keyStore.containsAlias(alias))
    androidKeyManager.delete(alias)
    assertFalse(keyStore.containsAlias(alias))
  }

  private fun createKey(template: EncrypterTemplate): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
        template.algorithm, providerName
    )
    keyGenerator.init(template.keyGenParameterSpec)
    return keyGenerator.generateKey()
  }
}