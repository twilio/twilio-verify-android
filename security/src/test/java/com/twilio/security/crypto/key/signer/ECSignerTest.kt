/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ECSignerTest {

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val signatureAlgorithm = "TestSignatureAlgorithm"

  private lateinit var ecSigner: ECSigner
  private lateinit var androidKeyStoreOperations: AndroidKeyStoreOperations

  @Before
  fun setup() {
    val keyPair: KeyPair = mock()
    val privateKey: PrivateKey = mock()
    val publicKey: PublicKey = mock()
    whenever(keyPair.private).thenReturn(privateKey)
    whenever(keyPair.public).thenReturn(publicKey)
    androidKeyStoreOperations = mock()
    ecSigner = ECSigner(keyPair, signatureAlgorithm, androidKeyStoreOperations)
  }

  @Test
  fun `Sign data using algorithm should return signature`() {
    val data = "test".toByteArray()
    val expectedSignature = "signature".toByteArray()
    whenever(androidKeyStoreOperations.sign(eq(data), eq(signatureAlgorithm), any())).thenReturn(
        expectedSignature
    )
    val signature = ecSigner.sign(data)
    assertTrue(expectedSignature.contentEquals(signature))
  }

  @Test
  fun `Error signing data should throw exception`() {
    val data = "test".toByteArray()
    val error: RuntimeException = mock()
    whenever(androidKeyStoreOperations.sign(eq(data), eq(signatureAlgorithm), any())).thenThrow(
        error
    )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    ecSigner.sign(data)
  }

  @Test
  fun `Get PublicKey should return expected key`() {
    val publicKey: PublicKey = mock()
    whenever(ecSigner.keyPair.public).thenReturn(publicKey)
    val expectedPublicKey = "publicKey"
    whenever(ecSigner.keyPair.public.encoded).thenReturn(
        expectedPublicKey.toByteArray()
    )

    assertTrue(
        ecSigner.getPublic()
            .contentEquals(expectedPublicKey.toByteArray())
    )
  }

  @Test(expected = KeyException::class)
  fun `Error getting PublicKey should throw exception`() {
    val publicKey: PublicKey = mock()
    whenever(ecSigner.keyPair.public).thenReturn(publicKey)
    val exception: KeyException = mock()
    given(ecSigner.keyPair.public).willAnswer {
      throw KeyException(exception)
    }
    ecSigner.getPublic()
  }

  @Test
  fun `Verify signature using algorithm should return true`() {
    val data = "test".toByteArray()
    val signature = "signature".toByteArray()
    val expectedResult = true
    whenever(
        androidKeyStoreOperations.verify(eq(data), eq(signature), eq(signatureAlgorithm), any())
    ).thenReturn(expectedResult)
    val result = ecSigner.verify(data, signature)
    assertEquals(expectedResult, result)
  }

  @Test
  fun `Error verifying signature should throw exception`() {
    val data = "test".toByteArray()
    val signature = "signature".toByteArray()
    val error: RuntimeException = mock()
    whenever(
        androidKeyStoreOperations.verify(eq(data), eq(signature), eq(signatureAlgorithm), any())
    ).thenThrow(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
        Matchers.instanceOf(
            RuntimeException::class.java
        )
    )
    ecSigner.verify(data, signature)
  }
}