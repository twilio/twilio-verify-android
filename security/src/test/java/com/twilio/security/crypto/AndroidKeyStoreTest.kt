/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

import android.security.keystore.KeyProperties
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.cipher.AlgorithmParametersSpec
import com.twilio.security.crypto.key.cipher.EncryptedData
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.mocks.cipher.CipherMockInput
import com.twilio.security.crypto.mocks.cipher.CipherMockOutput
import com.twilio.security.crypto.mocks.cipher.algorithmParametersMockName
import com.twilio.security.crypto.mocks.cipher.cipherMockInput
import com.twilio.security.crypto.mocks.cipher.cipherMockName
import com.twilio.security.crypto.mocks.cipher.cipherMockOutput
import com.twilio.security.crypto.mocks.keystore.KeyStoreMockInput
import com.twilio.security.crypto.mocks.keystore.KeyStoreMockOutput
import com.twilio.security.crypto.mocks.keystore.addProvider
import com.twilio.security.crypto.mocks.keystore.generator.keyGeneratorMockName
import com.twilio.security.crypto.mocks.keystore.generator.keyPairGeneratorMockName
import com.twilio.security.crypto.mocks.keystore.keyStoreMockInput
import com.twilio.security.crypto.mocks.keystore.keyStoreMockName
import com.twilio.security.crypto.mocks.keystore.keyStoreMockOutput
import com.twilio.security.crypto.mocks.keystore.setProviderAsVerified
import com.twilio.security.crypto.mocks.signature.SignatureMockInput
import com.twilio.security.crypto.mocks.signature.SignatureMockOutput
import com.twilio.security.crypto.mocks.signature.signatureMockInput
import com.twilio.security.crypto.mocks.signature.signatureMockName
import com.twilio.security.crypto.mocks.signature.signatureMockOutput
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.AlgorithmParameters
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Provider
import java.security.PublicKey
import java.security.Security
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import kotlin.random.Random.Default.nextBytes

@RunWith(RobolectricTestRunner::class)
class AndroidKeyStoreTest {

  private lateinit var key: Key
  private lateinit var publicKey: PublicKey
  private lateinit var privateKey: PrivateKey
  private lateinit var androidKeyStore: AndroidKeyStore
  private lateinit var provider: Provider

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val providerName = "TestKeyStore"
  private val signatureAlgorithm = "TestSignatureAlgorithm"
  private val cipherAlgorithm = "TestCipherAlgorithm"

  @Before
  fun setup() {
    provider = object : Provider(
        providerName, 1.0, "Fake KeyStore which is used for Robolectric tests"
    ) {
      init {
        put("KeyStore.$providerName", keyStoreMockName)
        put("KeyPairGenerator.${KeyProperties.KEY_ALGORITHM_EC}", keyPairGeneratorMockName)
        put("KeyGenerator.${KeyProperties.KEY_ALGORITHM_AES}", keyGeneratorMockName)
        put("Signature.$signatureAlgorithm", signatureMockName)
        put("Cipher.$cipherAlgorithm", cipherMockName)
        put("AlgorithmParameters.$cipherAlgorithm", algorithmParametersMockName)
      }
    }
    setProviderAsVerified(provider)
    addProvider(provider)
    keyStoreMockOutput = KeyStoreMockOutput()
    signatureMockInput = SignatureMockInput()
    signatureMockOutput = SignatureMockOutput()
    cipherMockInput = CipherMockInput()
    cipherMockOutput = CipherMockOutput()
    androidKeyStore =
      AndroidKeyStore(
          KeyStore.getInstance(providerName)
              .apply { load(null) }
      )
    val keyPair: KeyPair = mock()
    privateKey = mock()
    publicKey = mock()
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(keyPair.public).thenReturn(publicKey)
    key = mock()
  }

  @After
  fun tearDown() {
    Security.removeProvider(providerName)
  }

  @Test
  fun `Create a new key pair`() {
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val expectedKeyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val privateKey: PrivateKey = mock()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    whenever(expectedKeyPair.public).thenReturn(publicKey)
    whenever(expectedKeyPair.private).thenReturn(privateKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = expectedKeyPair, newKey = expectedKeyPair
      )
    val keyPair = androidKeyStore.createKeyPair(algorithm, mock())
    assertTrue(keyStoreMockOutput.generatedKeyPair)
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.public, keyPair?.public
    )
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.private, keyPair?.private
    )
  }

  @Test
  fun `Get an existing key pair`() {
    val alias = "test"
    val expectedKeyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val privateKey: PrivateKey = mock()
    whenever(expectedKeyPair.public).thenReturn(publicKey)
    whenever(expectedKeyPair.private).thenReturn(privateKey)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = expectedKeyPair
      )
    val keyPair = androidKeyStore.getKeyPair(alias)
    assertFalse(keyStoreMockOutput.generatedKeyPair)
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.public, keyPair?.public
    )
    assertEquals(
        (keyStoreMockInput.key as? KeyPair)?.private, keyPair?.private
    )
  }

  @Test
  fun `Existing key pair not found`() {
    val alias = "test"
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = null
      )
    assertTrue(androidKeyStore.contains(alias))
    assertNull(androidKeyStore.getKeyPair(alias))
  }

  @Test
  fun `Error getting an existing key pair`() {
    val alias = "test"
    val template: ECP256SignerTemplate = mock()
    val error: RuntimeException = mock()
    whenever(template.alias).thenReturn(alias)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = null, error = error
      )
    exceptionRule.expect(RuntimeException::class.java)
    androidKeyStore.getKeyPair(alias)
  }

  @Test
  fun `Create a new key`() {
    val algorithm = KeyProperties.KEY_ALGORITHM_AES
    val expectedKey: SecretKey = mock()
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = expectedKey, newKey = expectedKey
      )
    val key = androidKeyStore.createKey(algorithm, mock())
    assertTrue(keyStoreMockOutput.generatedKeyPair)
    assertEquals(keyStoreMockInput.key, key)
  }

  @Test
  fun `Get an existing key`() {
    val alias = "test"
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = mock<SecretKey>()
      )
    val key = androidKeyStore.getSecretKey(alias)
    assertFalse(keyStoreMockOutput.generatedKeyPair)
    assertEquals(keyStoreMockInput.key, key)
  }

  @Test
  fun `Synchronized key pair generation`() {
    val algorithm = KeyProperties.KEY_ALGORITHM_EC
    val expectedKeyPair: KeyPair = mock()
    val publicKey: PublicKey = mock()
    val privateKey: PrivateKey = mock()
    val encoded = ByteArray(5).apply { nextBytes(this) }
    val delay = 2
    val numThreads = 3
    val executor: ExecutorService = Executors.newFixedThreadPool(numThreads)
    whenever(expectedKeyPair.public).thenReturn(publicKey)
    whenever(expectedKeyPair.private).thenReturn(privateKey)
    whenever(publicKey.encoded).thenReturn(encoded)
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = false, key = expectedKeyPair, newKey = expectedKeyPair, delay = delay
      )
    for (i in 0..numThreads) {
      executor.submit {
        androidKeyStore.createKeyPair(algorithm, mock())
      }
    }
    executor.shutdown()
    executor.awaitTermination(delay * numThreads + delay.toLong(), TimeUnit.SECONDS)
    for (i in 0 until numThreads - 1) {
      assertTrue(
          keyStoreMockOutput.keyPairGenerationTimes[i + 1] - keyStoreMockOutput.keyPairGenerationTimes[i] >=
              TimeUnit.SECONDS.toMillis(delay.toLong())
      )
    }
  }

  @Test
  fun `Delete alias should delete it from keystore`() {
    val alias = "test"
    keyStoreMockInput =
      KeyStoreMockInput(
          containsAlias = true, key = mock<KeyPair>()
      )
    androidKeyStore.deleteEntry(alias)
    assertEquals(alias, keyStoreMockOutput.deletedAlias)
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
    exceptionRule.expect(RuntimeException::class.java)
    androidKeyStore.deleteEntry(alias)
  }

  @Test
  fun `Sign data using algorithm should return signature`() {
    val data = "test".toByteArray()
    val expectedSignature = "signature"
    signatureMockInput.signature = expectedSignature
    val signature = androidKeyStore.sign(data, signatureAlgorithm, privateKey)
    assertEquals(privateKey, signatureMockOutput.privateKey)
    assertTrue(signatureMockOutput.initialized)
    assertTrue(data.contentEquals(signatureMockOutput.updatedData!!))
    assertTrue(
        expectedSignature.toByteArray()
            .contentEquals(signature)
    )
  }

  @Test
  fun `Synchronized signature`() {
    val data = "test".toByteArray()
    val expectedSignature = "signature"
    val delay = 2
    val numThreads = 3
    val executor: ExecutorService = Executors.newFixedThreadPool(numThreads)
    signatureMockInput.signature = expectedSignature
    signatureMockInput.delay = 2
    for (i in 0..numThreads) {
      executor.submit {
        androidKeyStore.sign(data, signatureAlgorithm, privateKey)
      }
    }
    executor.shutdown()
    executor.awaitTermination(delay * numThreads + delay.toLong(), TimeUnit.SECONDS)
    for (i in 0 until numThreads - 1) {
      assertTrue(
          signatureMockOutput.signatureTimes[i + 1] - signatureMockOutput.signatureTimes[i] >=
              TimeUnit.SECONDS.toMillis(delay.toLong())
      )
    }
  }

  @Test
  fun `Error signing data should throw exception`() {
    val data = "test".toByteArray()
    val error: RuntimeException = mock()
    signatureMockInput.error = error
    exceptionRule.expect(RuntimeException::class.java)
    androidKeyStore.sign(data, signatureAlgorithm, privateKey)
  }

  @Test
  fun `Verify signature using algorithm should return true`() {
    val data = "test".toByteArray()
    val signature = "signature".toByteArray()
    val expectedResult = true
    signatureMockInput.result = expectedResult
    val result = androidKeyStore.verify(data, signature, signatureAlgorithm, publicKey)
    assertEquals(publicKey, signatureMockOutput.publicKey)
    assertTrue(signatureMockOutput.initialized)
    assertTrue(data.contentEquals(signatureMockOutput.updatedData!!))
    assertEquals(expectedResult, result)
  }

  @Test
  fun `Error verifying signature should throw exception`() {
    val data = "test".toByteArray()
    val signature = "signature".toByteArray()
    val error: RuntimeException = mock()
    signatureMockInput.error = error
    exceptionRule.expect(RuntimeException::class.java)
    androidKeyStore.verify(data, signature, signatureAlgorithm, publicKey)
  }

  @Test
  fun `Encrypt data using algorithm should return encrypted`() {
    val data = "test".toByteArray()
    val encrypted = "encrypted"
    val provider: Provider = mock {
      on { name }.doReturn(providerName)
    }
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { getProvider() }.doReturn(provider)
    }
    val expectedEncryptedData = EncryptedData(
        AlgorithmParametersSpec(
            algorithmParameters.encoded, algorithmParameters.provider.name,
            algorithmParameters.algorithm
        ), encrypted.toByteArray()
    )
    cipherMockInput.encrypted = encrypted
    cipherMockInput.algorithmParameters = algorithmParameters
    val encryptedData = androidKeyStore.encrypt(data, cipherAlgorithm, key)
    assertEquals(key, cipherMockOutput.secretKey)
    assertTrue(cipherMockOutput.cipherInitialized)
    assertEquals(expectedEncryptedData, encryptedData)
  }

  @Test
  fun `Synchronized encryption`() {
    val data = "test".toByteArray()
    val encrypted = "encrypted"
    val provider: Provider = mock {
      on { name }.doReturn(providerName)
    }
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { getProvider() }.doReturn(provider)
    }
    val delay = 2
    val numThreads = 3
    val executor: ExecutorService = Executors.newFixedThreadPool(numThreads)
    cipherMockInput.encrypted = encrypted
    cipherMockInput.algorithmParameters = algorithmParameters
    cipherMockInput.delay = delay
    for (i in 0..numThreads) {
      executor.submit {
        androidKeyStore.encrypt(data, cipherAlgorithm, key)
      }
    }
    executor.shutdown()
    executor.awaitTermination(delay * numThreads + delay.toLong(), TimeUnit.SECONDS)
    for (i in 0 until numThreads - 1) {
      assertTrue(
          cipherMockOutput.encryptionTimes[i + 1] - cipherMockOutput.encryptionTimes[i] >=
              TimeUnit.SECONDS.toMillis(delay.toLong())
      )
    }
  }

  @Test
  fun `Error encrypting data should throw exception`() {
    val data = "test".toByteArray()
    val error: RuntimeException = mock()
    cipherMockInput.error = error
    exceptionRule.expect(RuntimeException::class.java)
    androidKeyStore.encrypt(data, cipherAlgorithm, key)
  }

  @Test
  fun `Decrypt data using algorithm should return encrypted`() {
    val data = "test"
    val encrypted = "encrypted"
    val provider: Provider = mock {
      on { name }.doReturn(providerName)
    }
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { getProvider() }.doReturn(provider)
    }
    val expectedEncryptedData = EncryptedData(
        AlgorithmParametersSpec(
            algorithmParameters.encoded, algorithmParameters.provider.name,
            algorithmParameters.algorithm
        ), encrypted.toByteArray()
    )
    cipherMockInput.decrypted = data
    cipherMockInput.algorithmParameters = algorithmParameters
    val decrypted = androidKeyStore.decrypt(expectedEncryptedData, cipherAlgorithm, key)
    assertEquals(key, cipherMockOutput.secretKey)
    assertTrue(cipherMockOutput.cipherInitialized)
    assertTrue(
        data.toByteArray()
            .contentEquals(decrypted)
    )
  }

  @Test
  fun `Error decrypting data should throw exception`() {
    val encrypted = "encrypted"
    val provider: Provider = mock {
      on { name }.doReturn(providerName)
    }
    val algorithmParameters: AlgorithmParameters = mock {
      on { encoded }.doReturn(nextBytes(5))
      on { algorithm }.doReturn(cipherAlgorithm)
      on { getProvider() }.doReturn(provider)
    }
    val expectedEncryptedData = EncryptedData(
        AlgorithmParametersSpec(
            algorithmParameters.encoded, algorithmParameters.provider.name,
            algorithmParameters.algorithm
        ), encrypted.toByteArray()
    )
    val error: RuntimeException = mock()
    cipherMockInput.error = error
    exceptionRule.expect(RuntimeException::class.java)
    androidKeyStore.decrypt(expectedEncryptedData, cipherAlgorithm, key)
  }
}