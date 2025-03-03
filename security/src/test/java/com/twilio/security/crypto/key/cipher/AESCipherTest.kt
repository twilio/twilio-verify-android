/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.cipher

import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.key.authentication.Authenticator
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
  private var androidKeyStoreOperations: AndroidKeyStoreOperations = mockk()
  private var providerMock: Provider = mockk(relaxed = true) {
    every { name }.returns(providerName)
  }
  private val key: SecretKey = mockk()

  @Before
  fun setup() {
    aesCipher = AESCipher(key, cipherAlgorithm, androidKeyStoreOperations)
  }

  @Test
  fun `Encrypt data using algorithm should return encrypted`() {
    val data = "test".toByteArray()
    val encrypted = "encrypted".toByteArray()
    val algorithmParameters: AlgorithmParameters = mockk {
      every { encoded } returns nextBytes(5)
      every { algorithm } returns cipherAlgorithm
      every { provider } returns providerMock
    }

    val expectedEncryptedData = EncryptedData(
      AlgorithmParametersSpec(
        algorithmParameters.encoded, algorithmParameters.provider.name,
        algorithmParameters.algorithm
      ),
      encrypted
    )

    every { androidKeyStoreOperations.encrypt(eq(data), eq(cipherAlgorithm), any()) } returns expectedEncryptedData

    val encryptedData = aesCipher.encrypt(data)

    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test
  fun `Error encrypting data should throw exception`() {
    val data = "test".toByteArray()
    val error = RuntimeException()

    every { androidKeyStoreOperations.encrypt(eq(data), eq(cipherAlgorithm), any()) } throws error

    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(RuntimeException::class.java))
    aesCipher.encrypt(data)
  }

  @Test
  fun `Encrypt data with successful authentication should return encrypted`() {
    val data = "test".toByteArray()
    val expectedEncryptedData: EncryptedData = mockk()
    val authenticator: Authenticator = mockk()
    val cipher: Cipher = mockk()

    every { androidKeyStoreOperations.getCipherForEncryption(cipherAlgorithm, key) } returns cipher

    val authCallback = slot<(Cipher) -> Unit>()
    every { authenticator.startAuthentication(eq(cipher), capture(authCallback), any()) } answers {
      authCallback.captured.invoke(cipher)
    }

    every { androidKeyStoreOperations.encrypt(data, cipher) } returns expectedEncryptedData

    aesCipher.encrypt(
      data, authenticator,
      { encryptedData ->
        assertEquals(expectedEncryptedData, encryptedData)
      },
      { fail("Should not reach this failure callback") }
    )
  }

  @Test
  fun `Encrypt data with failed authentication should return error`() {
    val data = "test".toByteArray()
    val expectedError: RuntimeException = mockk()
    val authenticator: Authenticator = mockk()
    val cipher: Cipher = mockk()

    every { androidKeyStoreOperations.getCipherForEncryption(cipherAlgorithm, key) } returns cipher

    val errorCallback = slot<(Exception) -> Unit>()
    every { authenticator.startAuthentication(eq(cipher), any(), capture(errorCallback)) } answers {
      errorCallback.captured.invoke(expectedError)
    }

    aesCipher.encrypt(
      data,
      authenticator,
      { fail("Should not reach success callback") },
      { error -> assertEquals(expectedError, error) }
    )
  }

  @Test
  fun `Error encrypting data with exception from getting cipher for encryption should call error`() {
    val data = "test".toByteArray()
    val authenticator: Authenticator = mockk()
    val expectedError: RuntimeException = mockk()

    every { androidKeyStoreOperations.getCipherForEncryption(cipherAlgorithm, key) } throws expectedError

    aesCipher.encrypt(
      data,
      authenticator,
      { fail("Should not reach success callback") },
      { error -> assertEquals(expectedError, error) }
    )
  }

  @Test
  fun `Error encrypting data with successful authentication and error encrypting should call error`() {
    val data = "test".toByteArray()
    val authenticator: Authenticator = mockk()
    val cipher: Cipher = mockk()
    val expectedError: RuntimeException = mockk()

    every { androidKeyStoreOperations.getCipherForEncryption(cipherAlgorithm, key) } returns cipher

    val authenticationCallback = slot<(Cipher) -> Unit>()
    every { authenticator.startAuthentication(eq(cipher), capture(authenticationCallback), any()) } answers {
      authenticationCallback.captured.invoke(cipher)
    }

    every { androidKeyStoreOperations.encrypt(data, cipher) } throws expectedError

    aesCipher.encrypt(
      data,
      authenticator,
      { fail("Should not reach success callback") },
      { error -> assertEquals(expectedError, error) }
    )
  }

  @Test
  fun `Decrypt data with successful authentication should return decrypted`() {
    val encryptedData: EncryptedData = mockk()
    val expectedData = "test".toByteArray()
    val authenticator: Authenticator = mockk()
    val cipher: Cipher = mockk()

    every {
      androidKeyStoreOperations.getCipherForDecryption(cipherAlgorithm, key, encryptedData)
    } returns cipher

    val authenticationCallback = slot<(Cipher) -> Unit>()
    every { authenticator.startAuthentication(eq(cipher), capture(authenticationCallback), any()) } answers {
      authenticationCallback.captured.invoke(cipher)
    }

    every { androidKeyStoreOperations.decrypt(encryptedData, cipher) } returns expectedData

    aesCipher.decrypt(
      encryptedData,
      authenticator,
      { decryptedData ->
        assertEquals(expectedData, decryptedData)
      },
      { fail("Should not reach error callback") }
    )
  }

  @Test
  fun `Decrypt data with failed authentication should return error`() {
    val encryptedData: EncryptedData = mockk()
    val expectedError: RuntimeException = mockk()
    val authenticator: Authenticator = mockk()
    val cipher: Cipher = mockk()

    every {
      androidKeyStoreOperations.getCipherForDecryption(cipherAlgorithm, key, encryptedData)
    } returns cipher

    val errorCallback = slot<(Exception) -> Unit>()
    every { authenticator.startAuthentication(eq(cipher), any(), capture(errorCallback)) } answers {
      errorCallback.captured.invoke(expectedError)
    }

    aesCipher.decrypt(
      encryptedData,
      authenticator,
      { fail("Should not reach success callback") },
      { error -> assertEquals(expectedError, error) }
    )
  }

  @Test
  fun `Error decrypting data with exception from getting cipher for decryption should call error`() {
    val encryptedData: EncryptedData = mockk()
    val authenticator: Authenticator = mockk()
    val expectedError: RuntimeException = mockk()

    every {
      androidKeyStoreOperations.getCipherForDecryption(cipherAlgorithm, key, encryptedData)
    } throws expectedError

    aesCipher.decrypt(
      encryptedData,
      authenticator,
      { fail("Should not reach success callback") },
      { error -> assertEquals(expectedError, error) }
    )
  }

  @Test
  fun `Decrypt data using algorithm should return decrypted data`() {
    val data = "test"
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mockk {
      every { encoded } returns nextBytes(5)
      every { algorithm } returns cipherAlgorithm
      every { provider } returns providerMock
    }

    val expectedEncryptedData = EncryptedData(
      AlgorithmParametersSpec(
        algorithmParameters.encoded,
        algorithmParameters.provider.name,
        algorithmParameters.algorithm
      ),
      encrypted.toByteArray()
    )

    every {
      androidKeyStoreOperations.decrypt(expectedEncryptedData, cipherAlgorithm, any())
    } returns data.toByteArray()

    val decrypted = aesCipher.decrypt(expectedEncryptedData)

    assertTrue(data.toByteArray().contentEquals(decrypted))
  }

  @Test
  fun `Error decrypting data should throw exception`() {
    val encrypted = "encrypted"
    val algorithmParameters: AlgorithmParameters = mockk(relaxed = true) {
      every { encoded } returns nextBytes(5)
      every { algorithm } returns cipherAlgorithm
      every { provider } returns providerMock
    }

    val expectedEncryptedData = EncryptedData(
      AlgorithmParametersSpec(
        algorithmParameters.encoded,
        algorithmParameters.provider.name,
        algorithmParameters.algorithm
      ),
      encrypted.toByteArray()
    )

    val error = mockk<RuntimeException>(relaxed = true)

    every {
      androidKeyStoreOperations.decrypt(expectedEncryptedData, cipherAlgorithm, any())
    } throws error
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(RuntimeException::class.java))
    aesCipher.decrypt(expectedEncryptedData)
  }

  @Test
  fun `Error decrypting data with successful authentication and error decrypting should call error`() {
    val encryptedData: EncryptedData = mockk()
    val authenticator: Authenticator = mockk()
    val cipher: Cipher = mockk()
    val expectedError: RuntimeException = mockk()

    every {
      androidKeyStoreOperations.getCipherForDecryption(cipherAlgorithm, key, encryptedData)
    } returns cipher

    val authCaptor = slot<(Cipher) -> Unit>()
    every { authenticator.startAuthentication(eq(cipher), capture(authCaptor), any()) } answers {
      authCaptor.captured.invoke(cipher)
    }

    every { androidKeyStoreOperations.decrypt(encryptedData, cipher) } throws expectedError

    aesCipher.decrypt(
      encryptedData,
      authenticator,
      { fail("Expected error, but got success") },
      { error -> assertEquals(expectedError, error) }
    )
  }
}
