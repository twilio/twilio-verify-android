/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage.key

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.authentication.BiometricAuthenticator
import com.twilio.security.crypto.key.authentication.BiometricError
import com.twilio.security.crypto.key.cipher.AlgorithmParametersSpec
import com.twilio.security.crypto.key.cipher.Cipher
import com.twilio.security.crypto.key.cipher.EncryptedData
import com.twilio.security.crypto.key.cipher.fromByteArray
import com.twilio.security.crypto.key.cipher.toByteArray
import com.twilio.security.crypto.key.template.CipherTemplate
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.security.InvalidKeyException
import kotlin.random.Random.Default.nextBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BiometricSecretKeyTest {

  private val keyManager: KeyManager = mockk(relaxed = true)
  private val template: CipherTemplate = mockk(relaxed = true)
  private val biometricSecretKey: BiometricSecretKey = BiometricSecretKey(template, keyManager)

  @Test
  fun testCreate_shouldCallKeyManager() {
    biometricSecretKey.create()
    verify { keyManager.cipher(template.templateForCreation()) }
  }

  @Test
  fun testEncrypt_withData_shouldReturnEncrypted() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mockk(relaxed = true)
    val expectedEncryptedData = EncryptedData(AlgorithmParametersSpec(data, "test", "test"), data)
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher

    val successCallback = slot<(EncryptedData) -> Unit>()
    val errorCallback = slot<(Exception) -> Unit>()

    every {
      cipher.encrypt(eq(data), eq(authenticator), capture(successCallback), capture(errorCallback))
    } answers {
      successCallback.captured.invoke(expectedEncryptedData)
    }

    biometricSecretKey.encrypt(
      data, authenticator,
      {
        val encryptedData = fromByteArray(it)
        assertEquals(expectedEncryptedData, encryptedData)
      },
      { fail("Encryption should not fail") }
    )

    verify { cipher.encrypt(eq(data), eq(authenticator), any(), any()) }
  }

  @Test
  fun testEncrypt_withErrorEncrypting_shouldThrowError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mockk()
    val exception = mockk<KeyException>(relaxed = true)
    val authenticator: BiometricAuthenticator = mockk()
    every { keyManager.cipher(template) } returns cipher

    val errorCallback = slot<(Exception) -> Unit>()
    every {
      cipher.encrypt(eq(data), eq(authenticator), any(), capture(errorCallback))
    } answers {
      errorCallback.captured(exception)
    }

    biometricSecretKey.encrypt(
      data, authenticator,
      { fail("Encryption should not succeed") },
      {
        assertEquals(exception, it)
      }
    )

    verify { cipher.encrypt(eq(data), eq(authenticator), any(), any()) }
  }

  @Test
  fun testEncrypt_withKeyPermanentlyInvalidatedException_shouldThrowExpectedError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mockk()
    val exception = mockk<KeyPermanentlyInvalidatedException>()
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.encrypt(eq(data), eq(authenticator), any(), captureLambda()) } answers {
      lambda<(Exception) -> Unit>().captured.invoke(exception)
    }

    biometricSecretKey.encrypt(
      data, authenticator, { fail() },
      {
        assertEquals(BiometricError.KeyInvalidated.message, it.message)
      }
    )
  }

  @Test
  fun testEncrypt_withInvalidKeyException_shouldThrowExpectedError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mockk()
    val exception = mockk<InvalidKeyException>()
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.encrypt(eq(data), eq(authenticator), any(), captureLambda()) } answers {
      lambda<(Exception) -> Unit>().captured.invoke(exception)
    }

    biometricSecretKey.encrypt(
      data, authenticator, { fail() },
      {
        assertEquals(BiometricError.KeyInvalidated.message, it.message)
      }
    )
  }

  @Test
  fun testDecrypt_withData_shouldReturnDecrypted() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mockk()
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.decrypt(eq(encryptedData), eq(authenticator), captureLambda(), any()) } answers {
      lambda<(ByteArray) -> Unit>().captured.invoke(expectedData)
    }

    biometricSecretKey.decrypt(
      serializedEncryptedData, authenticator,
      {
        assertEquals(expectedData, it)
      },
      { fail() }
    )
  }

  @Test
  fun testDecrypt_withErrorEncrypting_shouldThrowError() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mockk()
    val exception = mockk<KeyException>()
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.decrypt(eq(encryptedData), eq(authenticator), any(), captureLambda()) } answers {
      lambda<(Exception) -> Unit>().captured.invoke(exception)
    }

    biometricSecretKey.decrypt(
      serializedEncryptedData, authenticator, { fail() },
      {
        assertEquals(exception, it)
      }
    )
  }

  @Test
  fun testDecrypt_withKeyPermanentlyInvalidatedException_shouldThrowExpectedError() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mockk()
    val exception = mockk<KeyPermanentlyInvalidatedException>()
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.decrypt(eq(encryptedData), eq(authenticator), any(), captureLambda()) } answers {
      lambda<(Exception) -> Unit>().captured.invoke(exception)
    }

    biometricSecretKey.decrypt(
      serializedEncryptedData, authenticator, { fail() },
      {
        assertEquals(BiometricError.KeyInvalidated.message, it.message)
      }
    )
  }

  @Test
  fun testDecrypt_withInvalidKeyException_shouldThrowExpectedError() {
    val expectedData = ByteArray(5).apply { nextBytes(this) }
    val encryptedData =
      EncryptedData(AlgorithmParametersSpec(expectedData, "test", "test"), expectedData)
    val serializedEncryptedData = toByteArray(encryptedData)
    val cipher: Cipher = mockk()
    val exception = mockk<InvalidKeyException>()
    val authenticator: BiometricAuthenticator = mockk()

    every { keyManager.cipher(template) } returns cipher
    every { cipher.decrypt(eq(encryptedData), eq(authenticator), any(), captureLambda()) } answers {
      lambda<(Exception) -> Unit>().captured.invoke(exception)
    }

    biometricSecretKey.decrypt(
      serializedEncryptedData, authenticator, { fail() },
      {
        assertEquals(BiometricError.KeyInvalidated.message, it.message)
      }
    )
  }

  @Test
  fun testDelete_shouldCallKeyManager() {
    val alias = "alias"
    every { template.alias }.returns(alias)
    biometricSecretKey.delete()
    verify { keyManager.delete(alias) }
  }
}
