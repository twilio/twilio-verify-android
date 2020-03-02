/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import android.security.keystore.KeyProperties
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.encrypter.AESEncrypter
import com.twilio.security.crypto.key.signer.ECSigner
import com.twilio.security.crypto.key.template.AESGCMNoPaddingEncrypterTemplate
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.mocks.keystore.KeyStoreMockInput
import com.twilio.security.crypto.mocks.keystore.KeyStoreMockOutput
import com.twilio.security.crypto.mocks.keystore.addProvider
import com.twilio.security.crypto.mocks.keystore.generator.keyGeneratorMockName
import com.twilio.security.crypto.mocks.keystore.generator.keyPairGeneratorMockName
import com.twilio.security.crypto.mocks.keystore.keyStoreMockInput
import com.twilio.security.crypto.mocks.keystore.keyStoreMockName
import com.twilio.security.crypto.mocks.keystore.keyStoreMockOutput
import com.twilio.security.crypto.mocks.keystore.setProviderAsVerified
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Provider
import java.security.PublicKey
import java.security.Security
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random.Default.nextBytes

@RunWith(RobolectricTestRunner::class)
class AndroidKeyManagerTest {

  private lateinit var androidKeyManager: KeyManager
  private lateinit var provider: Provider

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val providerName = "TestKeyStore"

  @Before
  fun setup() {
    provider = object : Provider(
        providerName, 1.0, "Fake KeyStore which is used for Robolectric tests"
    ) {
      init {
        put(
            "KeyStore.$providerName",
            keyStoreMockName
        )
        put(
            "KeyPairGenerator.${KeyProperties.KEY_ALGORITHM_EC}",
            keyPairGeneratorMockName
        )
        put(
            "KeyGenerator.${KeyProperties.KEY_ALGORITHM_AES}",
            keyGeneratorMockName
        )
      }
    }
    setProviderAsVerified(provider)
    addProvider(provider)
    keyStoreMockOutput = KeyStoreMockOutput()
    androidKeyManager =
      AndroidKeyManager(KeyStore.getInstance(providerName).apply { load(null) }, providerName)
  }

  @After
  fun tearDown() {
    Security.removeProvider(providerName)
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
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.signatureAlgorithm).thenReturn(signatureAlgorithm)
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = keyPair, newKey = keyPair
      )
    val signer = androidKeyManager.signer(template)
    assertTrue(keyStoreMockOutput.generatedKeyPair)
    assertTrue(signer is ECSigner)
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.public, (signer as? ECSigner)?.keyPair?.public
    )
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.private, (signer as? ECSigner)?.keyPair?.private
    )
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
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = keyPair
      )
    val signer = androidKeyManager.signer(template)
    assertFalse(keyStoreMockOutput.generatedKeyPair)
    assertTrue(signer is ECSigner)
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.public, (signer as? ECSigner)?.keyPair?.public
    )
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.private, (signer as? ECSigner)?.keyPair?.private
    )
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
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = null
      )
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
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(keyPair1.public).thenReturn(publicKey1)
    whenever(keyPair2.public).thenReturn(publicKey2)
    whenever(keyPair1.private).thenReturn(privateKey1)
    whenever(keyPair2.private).thenReturn(privateKey2)
    whenever(publicKey1.encoded).thenReturn(encoded1)
    whenever(publicKey2.encoded).thenReturn(encoded2)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = keyPair1, newKey = keyPair2
      )
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
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = null
      )
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
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = null
      )
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
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = null, error = error
      )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    androidKeyManager.signer(template)
  }

  @Test
  fun `Create a new AES encrypter`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_AES
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingEncrypterTemplate = mock()
    val key: SecretKey = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(template.parameterSpecClass).thenReturn(GCMParameterSpec::class.java)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = key, newKey = key
      )
    val encrypter = androidKeyManager.encrypter(template)
    assertTrue(keyStoreMockOutput.generatedKeyPair)
    assertTrue(encrypter is AESEncrypter)
    assertEquals(keyStoreMockInput.key, (encrypter as? AESEncrypter)?.key)
  }

  @Test
  fun `Get an existing AES encrypter`() {
    val alias = "test"
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingEncrypterTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(template.parameterSpecClass).thenReturn(GCMParameterSpec::class.java)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = mock<SecretKey>()
      )
    val encrypter = androidKeyManager.encrypter(template)
    assertFalse(keyStoreMockOutput.generatedKeyPair)
    assertTrue(encrypter is AESEncrypter)
    assertEquals(keyStoreMockInput.key, (encrypter as? AESEncrypter)?.key)
  }

  @Test
  fun `Different key for new AES encrypter`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_AES
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingEncrypterTemplate = mock()
    val key1: SecretKey = mock()
    val key2: SecretKey = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(template.parameterSpecClass).thenReturn(GCMParameterSpec::class.java)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = key1, newKey = key2
      )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalArgumentException::class.java
        )
    )
    androidKeyManager.encrypter(template)
  }

  @Test
  fun `Alias not found for previously created encrypter`() {
    val alias = "test"
    val template: AESGCMNoPaddingEncrypterTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.shouldExist).thenReturn(true)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = null
      )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            IllegalStateException::class.java
        )
    )
    androidKeyManager.encrypter(template)
  }

  @Test
  fun `Delete alias should delete it from keystore`() {
    val alias = "test"
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = mock<KeyPair>()
      )
    androidKeyManager.delete(alias)
    assertEquals(alias, keyStoreMockOutput.deletedAlias)
  }

  @Test
  fun `Delete non-existing alias should not call delete from keystore`() {
    val alias = "test"
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = null
      )
    androidKeyManager.delete(alias)
    assertTrue(keyStoreMockOutput.deletedAlias.isNullOrEmpty())
  }

  @Test
  fun `Error deleting alias should throw exception`() {
    val alias = "test"
    val error: RuntimeException = mock()
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = mock<KeyPair>(),
          error = error
      )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    androidKeyManager.delete(alias)
  }
}