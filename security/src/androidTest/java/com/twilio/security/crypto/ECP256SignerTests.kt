/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

class ECP256SignerTests {

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
    assertNotNull((signer as? ECSigner)?.keyPair)
    assertTrue(
        keyStore.getCertificate(alias)?.publicKey?.encoded?.contentEquals(
            (signer as ECSigner).keyPair.public.encoded
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
    assertNotNull((signer as? ECSigner)?.keyPair)
    assertTrue(
        keyPair.public.encoded?.contentEquals(
            (signer as ECSigner).keyPair.public.encoded
        ) == true
    )
  }

  @Test
  fun testSign_withSigner_shouldReturnSignature() {
    val data = "message".toByteArray()
    val template = ECP256SignerTemplate(alias)
    val keyPair = createKeyPair(template)
    val signer = androidKeyManager.signer(template)
    val signature = signer.sign(data)
    val valid = Signature.getInstance(template.signatureAlgorithm)
        .run {
          initVerify(keyPair.public)
          update(data)
          verify(signature)
        }
    assertTrue(valid)
  }

  @Test
  fun testGetPublicKey_shouldReturnKeyStorePublicKey() {
    val template = ECP256SignerTemplate(alias)
    val signer = androidKeyManager.signer(template)
    assertTrue(keyStore.containsAlias(alias))
    val expectedPublicKey = keyStore.getCertificate(alias)
        .publicKey.encoded
    assertTrue(signer.getPublic().contentEquals(expectedPublicKey))
  }

  @Test
  fun testVerify_withSigner_shouldReturnTrue() {
    val data = "message".toByteArray()
    val template = ECP256SignerTemplate(alias)
    val keyPair = createKeyPair(template)
    val signature = Signature.getInstance(template.signatureAlgorithm)
        .run {
          initSign(keyPair.private)
          update(data)
          sign()
        }
    val signer = androidKeyManager.signer(template)
    val valid = signer.verify(data, signature)
    assertTrue(valid)
  }

  @Test
  fun testDelete_withExistingKeyPair_shouldDeleteAlias() {
    val template = ECP256SignerTemplate(alias)
    createKeyPair(template)
    assertTrue(keyStore.containsAlias(alias))
    androidKeyManager.delete(alias)
    assertFalse(keyStore.containsAlias(alias))
  }

  private fun createKeyPair(template: SignerTemplate): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(
        template.algorithm, providerName
    )
    keyPairGenerator.initialize(template.keyGenParameterSpec)
    return keyPairGenerator.generateKeyPair()
  }
}