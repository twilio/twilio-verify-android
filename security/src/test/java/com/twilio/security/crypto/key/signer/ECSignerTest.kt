/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.signer

import com.twilio.security.crypto.AndroidKeyStoreOperations
import com.twilio.security.crypto.KeyException
import io.mockk.every
import io.mockk.mockk
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ECSignerTest {

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  private val signatureAlgorithm = "TestSignatureAlgorithm"

  private lateinit var ecSigner: ECSigner
  private lateinit var androidKeyStoreOperations: AndroidKeyStoreOperations

  @Before
  fun setup() {
    val keyPair: KeyPair = mockk(relaxed = true)
    val privateKey: PrivateKey = mockk(relaxed = true)
    val publicKey: PublicKey = mockk(relaxed = true)
    every { keyPair.private }.returns(privateKey)
    every { keyPair.public }.returns(publicKey)
    androidKeyStoreOperations = mockk()
    ecSigner = ECSigner(keyPair, signatureAlgorithm, androidKeyStoreOperations)
  }

  @Test
  fun `Sign data using algorithm should return signature`() {
    val data = "test".toByteArray()
    val expectedSignature = "signature".toByteArray()

    every { androidKeyStoreOperations.sign(eq(data), eq(signatureAlgorithm), any()) } returns expectedSignature

    val signature = ecSigner.sign(data)

    assertTrue(expectedSignature.contentEquals(signature))
  }

  @Test
  fun `Error signing data should throw exception`() {
    val data = "test".toByteArray()
    val error: RuntimeException = mockk(relaxed = true)
    every { androidKeyStoreOperations.sign(eq(data), eq(signatureAlgorithm), any()) }.throws(
      error
    )
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        RuntimeException::class.java
      )
    )
    ecSigner.sign(data)
  }

  @Test
  fun `Get PublicKey should return expected key`() {
    val publicKey: PublicKey = mockk()
    every { ecSigner.keyPair.public }.returns(publicKey)
    val expectedPublicKey = "publicKey"
    every { ecSigner.keyPair.public.encoded }.returns(
      expectedPublicKey.toByteArray()
    )

    assertTrue(
      ecSigner.getPublic()
        .contentEquals(expectedPublicKey.toByteArray())
    )
  }

  @Test(expected = KeyException::class)
  fun `Error getting PublicKey should throw exception`() {
    val publicKey: PublicKey = mockk()
    every { ecSigner.keyPair.public }.returns(publicKey)
    val exception: KeyException = mockk(relaxed = true)
    every { ecSigner.keyPair.public }.throws(exception)

    ecSigner.getPublic()
  }

  @Test
  fun `Verify signature using algorithm should return true`() {
    val data = "test".toByteArray()
    val signature = "signature".toByteArray()
    val expectedResult = true

    every { androidKeyStoreOperations.verify(eq(data), eq(signature), eq(signatureAlgorithm), any()) } returns expectedResult

    val result = ecSigner.verify(data, signature)

    assertEquals(expectedResult, result)
  }

  @Test
  fun `Error verifying signature should throw exception`() {
    val data = "test".toByteArray()
    val signature = "signature".toByteArray()
    val error: RuntimeException = mockk(relaxed = true)
    every {
      androidKeyStoreOperations.verify(eq(data), eq(signature), eq(signatureAlgorithm), any())
    }.throws(error)
    exceptionRule.expect(KeyException::class.java)
    exceptionRule.expectCause(
      Matchers.instanceOf<Throwable>(
        RuntimeException::class.java
      )
    )
    ecSigner.verify(data, signature)
  }
}
