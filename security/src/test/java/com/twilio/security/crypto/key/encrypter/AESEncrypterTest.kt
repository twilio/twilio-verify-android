/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.encrypter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.mocks.cipher.CipherMockInput
import com.twilio.security.crypto.mocks.cipher.CipherMockOutput
import com.twilio.security.crypto.mocks.cipher.cipherMockInput
import com.twilio.security.crypto.mocks.cipher.cipherMockName
import com.twilio.security.crypto.mocks.cipher.cipherMockOutput
import com.twilio.security.crypto.mocks.keystore.addProvider
import com.twilio.security.crypto.mocks.keystore.keyStoreMockName
import com.twilio.security.crypto.mocks.keystore.setProviderAsVerified
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.AlgorithmParameters
import java.security.Provider
import java.security.Security
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@RunWith(RobolectricTestRunner::class)
class AESEncrypterTest {

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val providerName = "TestKeyStore"
  private val cipherAlgorithm = "TestCipherAlgorithm"

  private lateinit var AESEncrypter: AESEncrypter
  private lateinit var provider: Provider

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
            "Cipher.$cipherAlgorithm",
            cipherMockName
        )
      }
    }
    setProviderAsVerified(provider)
    addProvider(provider)
    cipherMockInput = CipherMockInput()
    cipherMockOutput = CipherMockOutput()
    val key: SecretKey = mock()
    AESEncrypter = AESEncrypter(key, cipherAlgorithm, GCMParameterSpec::class.java)
  }

  @After
  fun tearDown() {
    Security.removeProvider(providerName)
  }

  @Test
  fun `Encrypt data using algorithm should return encrypted`() {
    val data = "test".toByteArray()
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mock()
    val gcmParameterSpec: GCMParameterSpec = mock()
    whenever(algorithmParameters.getParameterSpec(GCMParameterSpec::class.java)).thenReturn(
        gcmParameterSpec
    )
    val expectedEncryptedData = EncryptedData(gcmParameterSpec, encrypted.toByteArray())
    cipherMockInput.encrypted = encrypted
    cipherMockInput.algorithmParameters = algorithmParameters
    val encryptedData = AESEncrypter.encrypt(data)
    assertEquals(AESEncrypter.key, cipherMockOutput.secretKey)
    assertTrue(cipherMockOutput.cipherInitialized)
    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test
  fun `Error encrypting data should throw exception`() {
    val data = "test".toByteArray()
    val error: RuntimeException = mock()
    cipherMockInput.error = error
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    AESEncrypter.encrypt(data)
  }

  @Test
  fun `Decrypt data using algorithm should return encrypted`() {
    val data = "test"
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mock()
    val gcmParameterSpec: GCMParameterSpec = mock()
    whenever(algorithmParameters.getParameterSpec(GCMParameterSpec::class.java)).thenReturn(
        gcmParameterSpec
    )
    val encryptedData = EncryptedData(gcmParameterSpec, encrypted.toByteArray())
    cipherMockInput.decrypted = data
    cipherMockInput.algorithmParameters = algorithmParameters
    val decrypted = AESEncrypter.decrypt(encryptedData)
    assertEquals(AESEncrypter.key, cipherMockOutput.secretKey)
    assertTrue(cipherMockOutput.cipherInitialized)
    assertTrue(data.toByteArray().contentEquals(decrypted))
  }

  @Test
  fun `Error decrypting data should throw exception`() {
    val algorithmParameters: AlgorithmParameters = mock()
    val gcmParameterSpec: GCMParameterSpec = mock()
    whenever(algorithmParameters.getParameterSpec(GCMParameterSpec::class.java)).thenReturn(
        gcmParameterSpec
    )
    val encrypted = "encrypted"
    val encryptedData = EncryptedData(gcmParameterSpec, encrypted.toByteArray())
    val error: RuntimeException = mock()
    cipherMockInput.error = error
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    AESEncrypter.decrypt(encryptedData)
  }
}