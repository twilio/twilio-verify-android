/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.twilio.security.crypto.key.cipher.AESCipher
import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.template.AESGCMNoPaddingCipherTemplate
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey
import kotlin.random.Random.Default.nextBytes
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidKeyManagerTest {

  private lateinit var androidKeyManager: KeyManager
  private val androidKeyStore: AndroidKeyStore = mockk(relaxed = true)

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
    val template: ECP256SignerTemplate = mockk(relaxed = true)
    val keyPair: KeyPair = mockk()
    val publicKey: PublicKey = mockk()
    val privateKey: PrivateKey = mockk()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    val keyGenParameterSpec: KeyGenParameterSpec = mockk()
    every { template.alias }.returns(alias)
    every { template.algorithm }.returns(algorithm)
    every { template.signatureAlgorithm }.returns(signatureAlgorithm)
    every { template.keyGenParameterSpec }.returns(keyGenParameterSpec)
    every { keyPair.public }.returns(publicKey)
    every { keyPair.private }.returns(privateKey)
    every { publicKey.encoded }.returns(encoded)
    every { androidKeyStore.contains(alias) }.returnsMany(false, true)
    every { androidKeyStore.createKeyPair(algorithm, keyGenParameterSpec) }.returns(keyPair)
    every { androidKeyStore.getKeyPair(alias) }.returns(keyPair)
    val signer = androidKeyManager.signer(template)
    assertTrue(signer is ECSigner)
    assertEquals(keyPair.public, (signer as? ECSigner)?.keyPair?.public)
    assertEquals(keyPair.private, (signer as? ECSigner)?.keyPair?.private)
  }

  @Test
  fun `Get an existing EC signer`() {
    val alias = "test"
    val signatureAlgorithm = "signatureAlgorithm"
    val template: ECP256SignerTemplate = mockk()
    val keyPair: KeyPair = mockk()
    val publicKey: PublicKey = mockk()
    val privateKey: PrivateKey = mockk()
    every { keyPair.public }.returns(publicKey)
    every { keyPair.private }.returns(privateKey)
    every { template.alias }.returns(alias)
    every { template.signatureAlgorithm }.returns(signatureAlgorithm)
    every { androidKeyStore.contains(alias) }.returns(true)
    every { androidKeyStore.getKeyPair(alias) }.returns(keyPair)
    val signer = androidKeyManager.signer(template)
    assertTrue(signer is ECSigner)
    assertEquals(keyPair.public, (signer as? ECSigner)?.keyPair?.public)
    assertEquals(keyPair.private, (signer as? ECSigner)?.keyPair?.private)
  }

  @Test
  fun `New key pair for EC signer not found`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val template: ECP256SignerTemplate = mockk(relaxed = true)
    val keyPair: KeyPair = mockk()
    val publicKey: PublicKey = mockk()
    val privateKey: PrivateKey = mockk()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    val keyGenParameterSpec: KeyGenParameterSpec = mockk()
    every { template.alias }.returns(alias)
    every { template.algorithm }.returns(algorithm)
    every { template.keyGenParameterSpec }.returns(keyGenParameterSpec)
    every { keyPair.public }.returns(publicKey)
    every { keyPair.private }.returns(privateKey)
    every { publicKey.encoded }.returns(encoded)
    every { androidKeyStore.contains(alias) }.returnsMany(false, true)
    every { androidKeyStore.createKeyPair(algorithm, keyGenParameterSpec) }.returns(keyPair)
    every { androidKeyStore.getKeyPair(alias) }.returns(null)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Different key pair for new EC signer`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val template: ECP256SignerTemplate = mockk(relaxed = true)
    val keyPair1: KeyPair = mockk()
    val keyPair2: KeyPair = mockk()
    val publicKey1: PublicKey = mockk()
    val publicKey2: PublicKey = mockk()
    val privateKey1: PrivateKey = mockk()
    val privateKey2: PrivateKey = mockk()
    val encoded1 = ByteArray(5).apply { nextBytes(this) }
    val encoded2 = ByteArray(5).apply { nextBytes(this) }
    val keyGenParameterSpec: KeyGenParameterSpec = mockk()
    every { template.alias }.returns(alias)
    every { template.algorithm }.returns(algorithm)
    every { template.keyGenParameterSpec }.returns(keyGenParameterSpec)
    every { keyPair1.public }.returns(publicKey1)
    every { keyPair2.public }.returns(publicKey2)
    every { keyPair1.private }.returns(privateKey1)
    every { keyPair2.private }.returns(privateKey2)
    every { publicKey1.encoded }.returns(encoded1)
    every { publicKey2.encoded }.returns(encoded2)
    every { androidKeyStore.contains(alias) }.returnsMany(false, true)
    every { androidKeyStore.createKeyPair(algorithm, keyGenParameterSpec) }.returns(keyPair1)
    every { androidKeyStore.getKeyPair(alias) }.returns(keyPair2)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Existing key pair for EC signer not found`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mockk()
    every { template.alias }.returns(alias)
    every { androidKeyStore.contains(alias) }.returns(true)
    every { androidKeyStore.getKeyPair(alias) }.returns(null)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Alias not found for previously created signer`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mockk()
    every { template.alias }.returns(alias)
    every { template.shouldExist }.returns(true)
    every { androidKeyStore.contains(alias) }.returns(false)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalStateException::class.java
      )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Error getting an existing key pair for EC signer`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mockk()
    val error: RuntimeException = mockk(relaxed = true)
    every { template.alias }.returns(alias)
    every { androidKeyStore.contains(alias) }.returns(true)
    every { androidKeyStore.getKeyPair(alias) }.throws(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
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
    val template: AESGCMNoPaddingCipherTemplate = mockk(relaxed = true)
    val key: SecretKey = mockk()
    val keyGenParameterSpec: KeyGenParameterSpec = mockk()
    every { template.alias }.returns(alias)
    every { template.algorithm }.returns(algorithm)
    every { template.cipherAlgorithm }.returns(cipherAlgorithm)
    every { template.keyGenParameterSpec }.returns(keyGenParameterSpec)
    every { androidKeyStore.contains(alias) }.returnsMany(false, true)
    every { androidKeyStore.createKey(algorithm, keyGenParameterSpec) }.returns(key)
    every { androidKeyStore.getSecretKey(alias) }.returns(key)
    val cipher = androidKeyManager.cipher(template)
    assertTrue(cipher is AESCipher)
    assertEquals(key, (cipher as? AESCipher)?.key)
  }

  @Test
  fun `Get an existing AES cipher`() {
    val alias = "test"
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingCipherTemplate = mockk()
    val key: SecretKey = mockk()
    every { template.alias }.returns(alias)
    every { template.cipherAlgorithm }.returns(cipherAlgorithm)
    every { androidKeyStore.contains(alias) }.returns(true)
    every { androidKeyStore.getSecretKey(alias) }.returns(key)
    val cipher = androidKeyManager.cipher(template)
    assertTrue(cipher is AESCipher)
    assertEquals(key, (cipher as? AESCipher)?.key)
  }

  @Test
  fun `Different key for new AES cipher`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_AES
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingCipherTemplate = mockk(relaxed = true)
    val key1: SecretKey = mockk()
    val key2: SecretKey = mockk()
    val keyGenParameterSpec: KeyGenParameterSpec = mockk(relaxed = true)
    every { template.alias }.returns(alias)
    every { template.algorithm }.returns(algorithm)
    every { template.cipherAlgorithm }.returns(cipherAlgorithm)
    every { template.keyGenParameterSpec }.returns(keyGenParameterSpec)
    every { androidKeyStore.contains(alias) }.returnsMany(false, true)
    every { androidKeyStore.createKey(algorithm, keyGenParameterSpec) }.returns(key1)
    every { androidKeyStore.getSecretKey(alias) }.returns(key2)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    androidKeyManager.cipher(template)
  }

  @Test
  fun `Key not found for previously created cipher`() {
    val alias = "test"
    val template: AESGCMNoPaddingCipherTemplate = mockk()
    every { template.alias }.returns(alias)
    every { template.shouldExist }.returns(true)
    every { androidKeyStore.contains(alias) }.returns(true)
    every { androidKeyStore.getSecretKey(alias) }.returns(null)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalArgumentException::class.java
      )
    )
    androidKeyManager.cipher(template)
  }

  @Test
  fun `Alias not found for previously created cipher`() {
    val alias = "test"
    val template: AESGCMNoPaddingCipherTemplate = mockk()
    every { template.alias }.returns(alias)
    every { template.shouldExist }.returns(true)
    every { androidKeyStore.contains(alias) }.returns(false)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        IllegalStateException::class.java
      )
    )
    androidKeyManager.cipher(template)
  }

  @Test
  fun `Delete alias should delete it from keystore`() {
    val alias = "test"
    every { androidKeyStore.contains(alias) }.returns(true)
    androidKeyManager.delete(alias)
    verify { androidKeyStore.deleteEntry(alias) }
  }

  @Test
  fun `Delete non-existing alias should not call delete from keystore`() {
    val alias = "test"
    every { androidKeyStore.contains(alias) }.returns(false)
    androidKeyManager.delete(alias)
    verify(exactly = 0) { androidKeyStore.deleteEntry(alias) }
  }

  @Test
  fun `Error deleting alias should throw exception`() {
    val alias = "test"
    val error: RuntimeException = mockk(relaxed = true)
    every { androidKeyStore.contains(alias) }.returns(true)
    every { androidKeyStore.deleteEntry(alias) }.throws(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        RuntimeException::class.java
      )
    )
    androidKeyManager.delete(alias)
  }
}
