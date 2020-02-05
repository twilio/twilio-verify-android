/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import android.security.keystore.KeyProperties
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.encrypter.AESGCMEncrypter
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
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.SecretKeyEntry
import java.security.Provider
import java.security.PublicKey
import java.security.Security
import java.security.cert.Certificate
import javax.crypto.SecretKey
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
    val entry: PrivateKeyEntry = mock()
    val keyPair: KeyPair = mock()
    val certificate: Certificate = mock()
    val publicKey: PublicKey = mock()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.signatureAlgorithm).thenReturn(signatureAlgorithm)
    whenever(entry.certificate).thenReturn(certificate)
    whenever(certificate.publicKey).thenReturn(publicKey)
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, entry = entry, key = keyPair
      )
    val signer = androidKeyManager.signer(template)
    assertTrue(keyStoreMockOutput.generatedKeyPair)
    assertTrue(signer is ECSigner)
    assertEquals(keyStoreMockInput.entry, (signer as? ECSigner)?.entry)
  }

  @Test
  fun `Get an existing EC signer`() {
    val alias = "test"
    val signatureAlgorithm = "signatureAlgorithm"
    val template: ECP256SignerTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.signatureAlgorithm).thenReturn(signatureAlgorithm)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, entry = mock<PrivateKeyEntry>(), key = null
      )
    val signer = androidKeyManager.signer(template)
    assertFalse(keyStoreMockOutput.generatedKeyPair)
    assertTrue(signer is ECSigner)
    assertEquals(keyStoreMockInput.entry, (signer as? ECSigner)?.entry)
  }

  @Test
  fun `New entry for EC signer not found`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val template: ECP256SignerTemplate = mock()
    val keyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(keyPair.public).thenReturn(publicKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, entry = null, key = keyPair
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
  fun `Different entry for new EC signer`() {
    val alias = "test"
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val template: ECP256SignerTemplate = mock()
    val entry: PrivateKeyEntry = mock()
    val certificate: Certificate = mock()
    val keyPair: KeyPair = mock()
    val publicKey1: PublicKey = mock()
    val publicKey2: PublicKey = mock()
    val encoded1 = ByteArray(5).apply { nextBytes(this) }
    val encoded2 = ByteArray(5).apply { nextBytes(this) }
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(entry.certificate).thenReturn(certificate)
    whenever(certificate.publicKey).thenReturn(publicKey1)
    whenever(keyPair.public).thenReturn(publicKey2)
    whenever(publicKey1.encoded).thenReturn(encoded1)
    whenever(publicKey2.encoded).thenReturn(encoded2)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, entry = null, key = keyPair
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
  fun `Existing entry for EC signer not found`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, entry = null, key = null
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
  fun `Error getting an existing entry for EC signer`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mock()
    val error: RuntimeException = mock()
    whenever(template.alias).thenReturn(alias)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, entry = null, key = null, error = error
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
    val entry: SecretKeyEntry = mock()
    val key: SecretKey = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.algorithm).thenReturn(algorithm)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    whenever(entry.secretKey).thenReturn(key)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, entry = entry, key = key
      )
    val encrypter = androidKeyManager.encrypter(template)
    assertTrue(keyStoreMockOutput.generatedKeyPair)
    assertTrue(encrypter is AESGCMEncrypter)
    assertEquals(keyStoreMockInput.entry, (encrypter as? AESGCMEncrypter)?.entry)
  }

  @Test
  fun `Get an existing AES encrypter`() {
    val alias = "test"
    val cipherAlgorithm = "cipherAlgorithm"
    val template: AESGCMNoPaddingEncrypterTemplate = mock()
    whenever(template.alias).thenReturn(alias)
    whenever(template.cipherAlgorithm).thenReturn(cipherAlgorithm)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, entry = mock<SecretKeyEntry>(), key = null
      )
    val encrypter = androidKeyManager.encrypter(template)
    assertFalse(keyStoreMockOutput.generatedKeyPair)
    assertTrue(encrypter is AESGCMEncrypter)
    assertEquals(keyStoreMockInput.entry, (encrypter as? AESGCMEncrypter)?.entry)
  }
}