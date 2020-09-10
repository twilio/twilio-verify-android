/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.cipher.AESCipher
import com.twilio.security.crypto.key.cipher.AlgorithmParametersSpec
import com.twilio.security.crypto.key.cipher.EncryptedData
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.key.template.CipherTemplate
import java.security.AlgorithmParameters
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AESGCMNoPaddingCipherTests {

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
  fun testCipher_withNonExistingKey_shouldReturnCipherForNewKey() {
    val template = AESGCMNoPaddingCipherTemplate(alias).templateForCreation()
    val cipher = androidKeyManager.cipher(template)
    assertTrue(cipher is AESCipher)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((cipher as? AESCipher)?.key)
    assertEquals(keyStore.getKey(alias, null), (cipher as AESCipher).key)
  }

  @Test
  fun testCipher_withExistingKey_shouldReturnCipherForKey() {
    val template = AESGCMNoPaddingCipherTemplate(alias)
    val key = createKey(template)
    val cipher = androidKeyManager.cipher(template)
    assertTrue(cipher is AESCipher)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((cipher as? AESCipher)?.key)
    assertEquals(key, (cipher as AESCipher).key)
  }

  @Test
  fun testEncrypt_withCipher_shouldReturnEncryptedData() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingCipherTemplate(alias).templateForCreation()
    val cipher = androidKeyManager.cipher(template)
    val encryptedData = cipher.encrypt(data)
    val decrypted = cipher.decrypt(encryptedData)
    assertTrue(data.contentEquals(decrypted))
  }

  @Test
  fun testDecrypt_withCipher_shouldReturnData() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingCipherTemplate(alias).templateForCreation()
    val key = createKey(template)
    val encryptedData = Cipher.getInstance(template.cipherAlgorithm)
      .run {
        init(Cipher.ENCRYPT_MODE, key)
        EncryptedData(
          AlgorithmParametersSpec(
            parameters.encoded, parameters.provider.name, parameters.algorithm
          ),
          doFinal(data)
        )
      }
    val cipher = androidKeyManager.cipher(template)
    val decrypted = cipher.decrypt(encryptedData)
    assertTrue(data.contentEquals(decrypted))
  }

  @Test
  fun testEncrypt_withSameInput_shouldReturnDifferentEncryptedDataForEachEncrypt() {
    val data = "message".toByteArray()
    val template = AESGCMNoPaddingCipherTemplate(alias).templateForCreation()
    val cipher = androidKeyManager.cipher(template)
    val encryptedData1 = cipher.encrypt(data)
    val encryptedData2 = cipher.encrypt(data)
    assertNotEquals(encryptedData1, encryptedData2)
    val algorithmParametersSpec1 = AlgorithmParameters.getInstance(
      encryptedData1.algorithmParameters.algorithm,
      encryptedData1.algorithmParameters.provider
    )
      .apply {
        init(encryptedData1.algorithmParameters.encoded)
      }
      .getParameterSpec(GCMParameterSpec::class.java) as GCMParameterSpec
    val algorithmParametersSpec2 = AlgorithmParameters.getInstance(
      encryptedData2.algorithmParameters.algorithm,
      encryptedData2.algorithmParameters.provider
    )
      .apply {
        init(encryptedData2.algorithmParameters.encoded)
      }
      .getParameterSpec(GCMParameterSpec::class.java) as GCMParameterSpec
    assertFalse(algorithmParametersSpec1.iv!!.contentEquals(algorithmParametersSpec2.iv))
    assertEquals(algorithmParametersSpec1.tLen, algorithmParametersSpec2.tLen)
  }

  @Test
  fun testDelete_withExistingKeyPair_shouldDeleteAlias() {
    val template = AESGCMNoPaddingCipherTemplate(alias).templateForCreation()
    createKey(template)
    assertTrue(keyStore.containsAlias(alias))
    androidKeyManager.delete(alias)
    assertFalse(keyStore.containsAlias(alias))
  }

  @Test
  fun testCipher_withSameInput_shouldReturnDifferentEncryptedData() {
    val template = AESGCMNoPaddingCipherTemplate(alias).templateForCreation()
    val cipher = androidKeyManager.cipher(template)
    val text = "abcde"
    val encryptedData1 = cipher.encrypt(text.toByteArray())
    val encryptedData2 = cipher.encrypt(text.toByteArray())
    val encryptedData3 = cipher.encrypt(text.toByteArray())
    assertNotEquals(encryptedData1, encryptedData2)
    assertNotEquals(encryptedData1, encryptedData3)
    assertNotEquals(encryptedData2, encryptedData3)
  }

  private fun createKey(template: CipherTemplate): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
      template.algorithm, providerName
    )
    keyGenerator.init(template.keyGenParameterSpec)
    return keyGenerator.generateKey()
  }
}
