/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.util.Base64.NO_WRAP
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.KeyException
import com.twilio.security.crypto.KeyManager
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import kotlin.random.Random.Default.nextBytes
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class KeyStoreAdapterTest {

  private val keyManager: KeyManager = mock()
  private val keyStoreAdapter = KeyStoreAdapter(keyManager)

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Create keypair with valid signer should return encoded public key`() {
    val alias = "alias"
    val signer: Signer = mock()
    val publicKey: ByteArray = ByteArray(5).apply { nextBytes(this) }
    whenever(signer.getPublic()).thenReturn(publicKey)
    whenever(keyManager.signer(any())).thenReturn(signer)
    val encodedPublicKey = keyStoreAdapter.create(alias)
    assertEquals(encodeToBase64UTF8String(publicKey, NO_WRAP), encodedPublicKey)
  }

  @Test
  fun `Error creating signer should throw exception`() {
    val alias = "alias"
    whenever(keyManager.signer(any())).thenThrow(KeyException::class.java)
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(KeyException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(KeyStorageError))
    keyStoreAdapter.create(alias)
  }

  @Test
  fun `Sign and encode message with valid signer should return encoded signature`() {
    val alias = "alias"
    val message = "message"
    val signer: Signer = mock()
    val signature: ByteArray = ByteArray(5).apply { nextBytes(this) }
    whenever(signer.sign(message.toByteArray())).thenReturn(signature)
    whenever(keyManager.signer(any())).thenReturn(signer)
    val encodedSignature = keyStoreAdapter.signAndEncode(alias, message)
    assertEquals(encodeToBase64UTF8String(signature, NO_WRAP), encodedSignature)
  }

  @Test
  fun `Sign message with valid signer should return signature`() {
    val alias = "alias"
    val message = "message"
    val signer: Signer = mock()
    val expectedSignature: ByteArray = ByteArray(5).apply { nextBytes(this) }
    whenever(signer.sign(message.toByteArray())).thenReturn(expectedSignature)
    whenever(keyManager.signer(any())).thenReturn(signer)
    val signature = keyStoreAdapter.sign(alias, message)
    assertEquals(expectedSignature, signature)
  }

  @Test
  fun `Error signing and encoding message should throw exception`() {
    val alias = "alias"
    val message = "message"
    val signer: Signer = mock()
    whenever(keyManager.signer(any())).thenReturn(signer)
    whenever(signer.sign(message.toByteArray())).thenThrow(KeyException::class.java)
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(KeyException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(KeyStorageError))
    keyStoreAdapter.signAndEncode(alias, message)
  }

  @Test
  fun `Error signing message should throw exception`() {
    val alias = "alias"
    val message = "message"
    val signer: Signer = mock()
    whenever(keyManager.signer(any())).thenReturn(signer)
    whenever(signer.sign(message.toByteArray())).thenThrow(KeyException::class.java)
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(KeyException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(KeyStorageError))
    keyStoreAdapter.sign(alias, message)
  }

  @Test
  fun `Delete a signer with existing keypair should delete it`() {
    val alias = "alias"
    keyStoreAdapter.delete(alias)
    verify(keyManager).delete(alias)
  }

  @Test
  fun `Error deleting signer should throw exception`() {
    val alias = "alias"
    whenever(keyManager.delete(alias)).thenThrow(KeyException::class.java)
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(KeyException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(KeyStorageError))
    keyStoreAdapter.delete(alias)
  }
}
