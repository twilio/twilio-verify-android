/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.cipher.AESCipher
import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey
import kotlin.random.Random.Default.nextBytes

@RunWith(RobolectricTestRunner::class)
class AndroidKeyManagerTest {

  private lateinit var androidKeyManager: KeyManager
  private val androidKeyStore: AndroidKeyStore = mock()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Before
  fun setup() {
    androidKeyManager = AndroidKeyManager(androidKeyStore)
  }

  @Test
  fun `Create a new EC signer`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val signatureAlgorithm = "signatureAlgorithm"
    val template: ECP256SignerTemplate = mock()
    val keyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val privateKey: PrivateKey = mock()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    val keyGenParameterSpec: KeyGenParameterSpec = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.signatureAlgorithm).thenReturn(signatureAlgorithm)
    whenever(template.keyGenParameterSpec).thenReturn(keyGenParameterSpec)
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
        .thenReturn(true)
    whenever(androidKeyStore.createKeyPair(algorithm, keyGenParameterSpec)).thenReturn(keyPair)
    whenever(androidKeyStore.getKeyPair(alias)).thenReturn(keyPair)
    val signer = androidKeyManager.signer(template)
    assertTrue(signer is ECSigner)
    assertEquals(keyPair.public, (signer as? ECSigner)?.keyPair?.public)
    assertEquals(keyPair.private, (signer as? ECSigner)?.keyPair?.private)
  }

  @Test
  fun `Get an existing EC signer`() {
    val alias = "test"
    val signatureAlgorithm = "signatureAlgorithm"
    val template: ECP256SignerTemplate = mock()
    val keyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val privateKey: PrivateKey = mock()
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(template.alias).thenReturn(alias)
    whenever(template.signatureAlgorithm).thenReturn(signatureAlgorithm)
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    whenever(androidKeyStore.getKeyPair(alias)).thenReturn(keyPair)
    val signer = androidKeyManager.signer(template)
    assertTrue(signer is ECSigner)
    assertEquals(keyPair.public, (signer as? ECSigner)?.keyPair?.public)
    assertEquals(keyPair.private, (signer as? ECSigner)?.keyPair?.private)
  }

  @Test
  fun `New key pair for EC signer not found`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val template: ECP256SignerTemplate = mock()
    val keyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val privateKey: PrivateKey = mock()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    val keyGenParameterSpec: KeyGenParameterSpec = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.keyGenParameterSpec).thenReturn(keyGenParameterSpec)
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
        .thenReturn(true)
    whenever(androidKeyStore.createKeyPair(algorithm, keyGenParameterSpec)).thenReturn(keyPair)
    whenever(androidKeyStore.getKeyPair(alias)).thenReturn(null)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalArgumentException::class.java
        )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Different key pair for new EC signer`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val template: ECP256SignerTemplate = mock()
    val keyPair1: KeyPair = mock()
    val keyPair2: KeyPair = mock()
    val publicKey1: PublicKey = mock()
    val publicKey2: PublicKey = mock()
    val privateKey1: PrivateKey = mock()
    val privateKey2: PrivateKey = mock()
    val encoded1 = ByteArray(5).apply { nextBytes(this) }
    val encoded2 = ByteArray(5).apply { nextBytes(this) }
    val keyGenParameterSpec: KeyGenParameterSpec = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.keyGenParameterSpec).thenReturn(keyGenParameterSpec)
    whenever(keyPair1.public).thenReturn(publicKey1)
    whenever(keyPair2.public).thenReturn(publicKey2)
    whenever(keyPair1.private).thenReturn(privateKey1)
    whenever(keyPair2.private).thenReturn(privateKey2)
    whenever(publicKey1.encoded).thenReturn(encoded1)
    whenever(publicKey2.encoded).thenReturn(encoded2)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
        .thenReturn(true)
    whenever(androidKeyStore.createKeyPair(algorithm, keyGenParameterSpec)).thenReturn(keyPair1)
    whenever(androidKeyStore.getKeyPair(alias)).thenReturn(keyPair2)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalArgumentException::class.java
        )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Existing key pair for EC signer not found`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    whenever(androidKeyStore.getKeyPair(alias)).thenReturn(null)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalArgumentException::class.java
        )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Alias not found for previously created signer`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.shouldExist).thenReturn(true)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalStateException::class.java
        )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Error getting an existing key pair for EC signer`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mock()
    val error: RuntimeException = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    whenever(androidKeyStore.getKeyPair(alias)).thenThrow(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Create a new AES cipher`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_AES
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingCipherTemplate = mock()
    val key: SecretKey = mock()
    val keyGenParameterSpec: KeyGenParameterSpec = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(template.keyGenParameterSpec).thenReturn(keyGenParameterSpec)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
        .thenReturn(true)
    whenever(androidKeyStore.createKey(algorithm, keyGenParameterSpec)).thenReturn(key)
    whenever(androidKeyStore.getSecretKey(alias)).thenReturn(key)
    val cipher = androidKeyManager.cipher(template)
    assertTrue(cipher is AESCipher)
    assertEquals(key, (cipher as? AESCipher)?.key)
  }

  @Test
  fun `Get an existing AES cipher`() {
    val alias = "test"
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingCipherTemplate = mock()
    val key: SecretKey = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    whenever(androidKeyStore.getSecretKey(alias)).thenReturn(key)
    val cipher = androidKeyManager.cipher(template)
    assertTrue(cipher is AESCipher)
    assertEquals(key, (cipher as? AESCipher)?.key)
  }

  @Test
  fun `Different key for new AES cipher`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_AES
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingCipherTemplate = mock()
    val key1: SecretKey = mock()
    val key2: SecretKey = mock()
    val keyGenParameterSpec: KeyGenParameterSpec = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(template.keyGenParameterSpec).thenReturn(keyGenParameterSpec)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
        .thenReturn(true)
    whenever(androidKeyStore.createKey(algorithm, keyGenParameterSpec)).thenReturn(key1)
    whenever(androidKeyStore.getSecretKey(alias)).thenReturn(key2)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalArgumentException::class.java
        )
    )
    androidKeyManager.cipher(template)
  }

  @Test
  fun `Key not found for previously created cipher`() {
    val alias = "test"
    val template: AESGCMNoPaddingCipherTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.shouldExist).thenReturn(true)
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    whenever(androidKeyStore.getSecretKey(alias)).thenReturn(null)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalArgumentException::class.java
        )
    )
    androidKeyManager.cipher(template)
  }

  @Test
  fun `Alias not found for previously created cipher`() {
    val alias = "test"
    val template: AESGCMNoPaddingCipherTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.shouldExist).thenReturn(true)
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalStateException::class.java
        )
    )
    androidKeyManager.cipher(template)
  }

  @Test
  fun `Delete alias should delete it from keystore`() {
    val alias = "test"
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    androidKeyManager.delete(alias)
    verify(androidKeyStore).deleteEntry(alias)
  }

  @Test
  fun `Delete non-existing alias should not call delete from keystore`() {
    val alias = "test"
    whenever(androidKeyStore.contains(alias)).thenReturn(false)
    androidKeyManager.delete(alias)
    verify(androidKeyStore, never()).deleteEntry(alias)
  }

  @Test
  fun `Error deleting alias should throw exception`() {
    val alias = "test"
    val error: RuntimeException = mock()
    whenever(androidKeyStore.contains(alias)).thenReturn(true)
    whenever(androidKeyStore.deleteEntry(alias)).thenThrow(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    androidKeyManager.delete(alias)
  }
}