/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.cipher

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.key.authentication.Authenticator
import java.security.AlgorithmParameters
import java.security.Provider
import javax.crypto.Cipher
import javax.crypto.SecretKey
import kotlin.random.Random.Default.nextBytes
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AESCipherTest {

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val providerName = "TestKeyStore"
  private val cipherAlgorithm = "TestCipherAlgorithm"

  private lateinit var aesCipher: AESCipher
  private var androidKeyStoreOperations: AndroidKeyStoreOperations = mock()
  private var provider: Provider = mock {
    on { name }.doReturn(providerName)
  }
  private val key: SecretKey = mock()

  @Before
  fun setup() {
    aesCipher = AESCipher(key, cipherAlgorithm, androidKeyStoreOperations)
  }

  @Test
  fun `Encrypt data using algorithm should return encrypted`() {
    val data = "test".toByteArray()
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { provider }.doReturn(provider)
    }
    val expectedEncryptedData = EncryptedData(
      AlgorithmParametersSpec(
        algorithmParameters.encoded, algorithmParameters.provider.name,
        algorithmParameters.algorithm
      ),
      encrypted.toByteArray()
    )
    whenever(androidKeyStoreOperations.encrypt(eq(data), eq(cipherAlgorithm), any())).thenReturn(
      expectedEncryptedData
    )
    val encryptedData = aesCipher.encrypt(data)
    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test
  fun `Error encrypting data should throw exception`() {
    val data = "test".toByteArray()
    val error: RuntimeException = mock()
    whenever(androidKeyStoreOperations.encrypt(eq(data), eq(cipherAlgorithm), any())).thenThrow(
      error
    )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf(
        RuntimeException::class.java
      )
    )
    aesCipher.encrypt(data)
  }

  @Test
  fun `Encrypt data with successful authentication should return encrypted`() {
    val data = "test".toByteArray()
    val expectedEncryptedData: EncryptedData = mock()
    val authenticator: Authenticator = mock()
    val cipher: Cipher = mock()
    whenever(androidKeyStoreOperations.getCipherForEncryption(eq(cipherAlgorithm), eq(key))).thenReturn(
      cipher
    )
    argumentCaptor<(Cipher) -> Unit>().apply {
      whenever(authenticator.startAuthentication(eq(cipher), capture(), any())).then {
        firstValue.invoke(cipher)
      }
    }
    whenever(androidKeyStoreOperations.encrypt(eq(data), eq(cipher))).thenReturn(expectedEncryptedData)
    aesCipher.encrypt(
      data, authenticator,
      { encryptedData ->
        assertEquals(expectedEncryptedData, encryptedData)
      },
      { fail() }
    )
  }

  @Test
  fun `Encrypt data with failed authentication should return error`() {
    val data = "test".toByteArray()
    val expectedError: RuntimeException = mock()
    val authenticator: Authenticator = mock()
    val cipher: Cipher = mock()
    whenever(androidKeyStoreOperations.getCipherForEncryption(eq(cipherAlgorithm), eq(key))).thenReturn(
      cipher
    )
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(authenticator.startAuthentication(eq(cipher), any(), capture())).then {
        firstValue.invoke(expectedError)
      }
    }
    aesCipher.encrypt(data, authenticator, { fail() }, { error -> assertEquals(expectedError, error) })
  }

  @Test
  fun `Error encrypting data with exception from getting cipher for encryption should call error`() {
    val data = "test".toByteArray()
    val authenticator: Authenticator = mock()
    val expectedError: RuntimeException = mock()
    whenever(androidKeyStoreOperations.getCipherForEncryption(eq(cipherAlgorithm), eq(key))).thenThrow(
      expectedError
    )
    aesCipher.encrypt(data, authenticator, { fail() }, { error -> assertEquals(expectedError, error) })
  }

  @Test
  fun `Decrypt data with successful authentication should return encrypted`() {
    val encryptedData: EncryptedData = mock()
    val expectedData = "test".toByteArray()
    val authenticator: Authenticator = mock()
    val cipher: Cipher = mock()
    whenever(androidKeyStoreOperations.getCipherForDecryption(eq(cipherAlgorithm), eq(key), eq(encryptedData))).thenReturn(
      cipher
    )
    argumentCaptor<(Cipher) -> Unit>().apply {
      whenever(authenticator.startAuthentication(eq(cipher), capture(), any())).then {
        firstValue.invoke(cipher)
      }
    }
    whenever(androidKeyStoreOperations.decrypt(eq(encryptedData), eq(cipher))).thenReturn(expectedData)
    aesCipher.decrypt(
      encryptedData, authenticator,
      { decryptedData ->
        assertEquals(expectedData, decryptedData)
      },
      { fail() }
    )
  }

  @Test
  fun `Decrypt data with failed authentication should return error`() {
    val encryptedData: EncryptedData = mock()
    val expectedError: RuntimeException = mock()
    val authenticator: Authenticator = mock()
    val cipher: Cipher = mock()
    whenever(androidKeyStoreOperations.getCipherForDecryption(eq(cipherAlgorithm), eq(key), eq(encryptedData))).thenReturn(
      cipher
    )
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(authenticator.startAuthentication(eq(cipher), any(), capture())).then {
        firstValue.invoke(expectedError)
      }
    }
    aesCipher.decrypt(encryptedData, authenticator, { fail() }, { error -> assertEquals(expectedError, error) })
  }

  @Test
  fun `Error decrypting data with exception from getting cipher for encryption should call error`() {
    val encryptedData: EncryptedData = mock()
    val authenticator: Authenticator = mock()
    val expectedError: RuntimeException = mock()
    whenever(androidKeyStoreOperations.getCipherForDecryption(eq(cipherAlgorithm), eq(key), eq(encryptedData))).thenThrow(
      expectedError
    )
    aesCipher.decrypt(encryptedData, authenticator, { fail() }, { error -> assertEquals(expectedError, error) })
  }

  @Test
  fun `Decrypt data using algorithm should return encrypted`() {
    val data = "test"
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { provider }.doReturn(provider)
    }
    val expectedEncryptedData = EncryptedData(
      AlgorithmParametersSpec(
        algorithmParameters.encoded, algorithmParameters.provider.name,
        algorithmParameters.algorithm
      ),
      encrypted.toByteArray()
    )
    whenever(
      androidKeyStoreOperations.decrypt(eq(expectedEncryptedData), eq(cipherAlgorithm), any())
    ).thenReturn(data.toByteArray())
    val decrypted = aesCipher.decrypt(expectedEncryptedData)
    assertTrue(
      data.toByteArray()
        .contentEquals(decrypted)
    )
  }

  @Test
  fun `Error decrypting data should throw exception`() {
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { provider }.doReturn(provider)
    }
    val expectedEncryptedData = EncryptedData(
      AlgorithmParametersSpec(
        algorithmParameters.encoded, algorithmParameters.provider.name,
        algorithmParameters.algorithm
      ),
      encrypted.toByteArray()
    )
    val error: RuntimeException = mock()
    whenever(
      androidKeyStoreOperations.decrypt(eq(expectedEncryptedData), eq(cipherAlgorithm), any())
    ).thenThrow(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf(
        RuntimeException::class.java
      )
    )
    aesCipher.decrypt(expectedEncryptedData)
  }
}
