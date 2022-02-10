/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.storage.key

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
import java.security.InvalidKeyException
import kotlin.random.Random.Default.nextBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BiometricSecretKeyTest {

  private val keyManager: KeyManager = mock()
  private val template: CipherTemplate = mock()
  private val biometricSecretKey: BiometricSecretKey = BiometricSecretKey(template, keyManager)

  @Test
  fun testCreate_shouldCallKeyManager() {
    biometricSecretKey.create()
    verify(keyManager).cipher(template.templateForCreation())
  }

  @Test
  fun testEncrypt_withData_shouldReturnEncrypted() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mock()
    val expectedEncryptedData = EncryptedData(AlgorithmParametersSpec(data, "test", "test"), data)
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(EncryptedData) -> Unit>().apply {
      whenever(cipher.encrypt(eq(data), eq(authenticator), capture(), any())).then {
        firstValue.invoke(expectedEncryptedData)
      }
    }
    biometricSecretKey.encrypt(
      data, authenticator,
      {
        val encryptedData = fromByteArray(it)
        assertEquals(expectedEncryptedData, encryptedData)
      },
      { fail() }
    )
  }

  @Test
  fun testEncrypt_withErrorEncrypting_shouldThrowError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mock()
    val exception: KeyException = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(cipher.encrypt(eq(data), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
    }
    biometricSecretKey.encrypt(
      data, authenticator, { fail() },
      {
        assertEquals(exception, it)
      }
    )
  }

  @Test
  fun testEncrypt_withKeyPermanentlyInvalidatedException_shouldThrowExpectedError() {
    val data = ByteArray(5).apply { nextBytes(this) }
    val cipher: Cipher = mock()
    val exception: KeyPermanentlyInvalidatedException = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(cipher.encrypt(eq(data), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
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
    val cipher: Cipher = mock()
    val exception: InvalidKeyException = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(cipher.encrypt(eq(data), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
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
    val cipher: Cipher = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(ByteArray) -> Unit>().apply {
      whenever(cipher.decrypt(eq(encryptedData), eq(authenticator), capture(), any())).then {
        firstValue.invoke(expectedData)
      }
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
    val cipher: Cipher = mock()
    val exception: KeyException = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(cipher.decrypt(eq(encryptedData), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
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
    val cipher: Cipher = mock()
    val exception: KeyPermanentlyInvalidatedException = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(cipher.decrypt(eq(encryptedData), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
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
    val cipher: Cipher = mock()
    val exception: KeyPermanentlyInvalidatedException = mock()
    val authenticator: BiometricAuthenticator = mock()
    whenever(keyManager.cipher(template)).thenReturn(cipher)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(cipher.decrypt(eq(encryptedData), eq(authenticator), any(), capture())).then {
        firstValue.invoke(exception)
      }
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
    whenever(template.alias).thenReturn(alias)
    biometricSecretKey.delete()
    verify(keyManager).delete(alias)
  }
}
