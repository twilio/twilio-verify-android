/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore

class SignerTests {

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
  fun testSigner_withNonExistingKeyPair_shouldReturnSignerForNewKeyPair() {
    val template = ECP256SignerTemplate(alias)
    val signer = androidKeyManager.signer(template)
    assertTrue(signer is ECSigner)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((signer as? ECSigner)?.entry)
    assertTrue(
        (keyStore.getEntry(
            alias, null
        ) as? KeyStore.PrivateKeyEntry)?.certificate?.publicKey?.encoded?.contentEquals(
            (signer as ECSigner).entry.certificate.publicKey.encoded
        ) == true
    )
  }

  @Test
  fun testSigner_withExistingKeyPair_shouldReturnSignerForKeyPair() {
    val template = ECP256SignerTemplate(alias)
    val keyPair = createKeyPair(template)
    val signer = androidKeyManager.signer(template)
    assertTrue(signer is ECSigner)
    assertTrue(keyStore.containsAlias(alias))
    assertNotNull((signer as? ECSigner)?.entry)
    assertTrue(
        keyPair.public.encoded?.contentEquals(
            (signer as ECSigner).entry.certificate.publicKey.encoded
        ) == true
    )
  }

  private fun createKeyPair(template: SignerTemplate): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(
        template.algorithm, providerName
    )
    keyPairGenerator.initialize(template.keyGenParameterSpec)
    return keyPairGenerator.generateKeyPair()
  }
}