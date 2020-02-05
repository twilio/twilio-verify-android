/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.encrypter.AESEncrypter
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
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EncrypterTests {

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

  private fun createKey(template: EncrypterTemplate): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
        template.algorithm, providerName
    )
    keyGenerator.init(template.keyGenParameterSpec)
    return keyGenerator.generateKey()
  }
}