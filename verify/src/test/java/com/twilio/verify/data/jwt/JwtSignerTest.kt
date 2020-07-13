/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data.jwt

import android.util.Base64
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.encodeToBase64UTF8String
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random.Default.nextBytes
import kotlin.random.Random.Default.nextInt

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class JwtSignerTest {

  private val keyStorage: KeyStorage = mock()
  private val jwtSigner = JwtSigner(keyStorage)

  @Test
  fun `Sign a jwt content with EC256 signer should return signature`() {
    val content = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
        "G4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val signerTemplate: ECP256SignerTemplate = mock()
    val alias = "test"
    whenever(signerTemplate.alias).thenReturn(alias)
    val derSignature = "MEQCIFtun9Ioo-W-juCG7sOl8PPPuozb8cspsUtpu2TxnzP_AiAi1VpFNTr2eK-VX3b1DLHy8" +
        "rPm3MOpTvUH14hyNr0Gfg"
    val concatSignature = "W26f0iij5b6O4Ibuw6Xw88-6jNvxyymxS2m7ZPGfM_8i1VpFNTr2eK-VX3b1DLHy8rPm3M" +
        "OpTvUH14hyNr0Gfg"
    whenever(keyStorage.sign(alias, content)).thenReturn(Base64.decode(derSignature, FLAGS))
    val signature = jwtSigner.sign(signerTemplate, content)
    assertEquals(concatSignature, encodeToBase64UTF8String(signature, FLAGS))
  }

  @Test
  fun `Sign a jwt content with signer should return signature`() {
    val content = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
        "G4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val signerTemplate: SignerTemplate = mock()
    val alias = "test"
    whenever(signerTemplate.alias).thenReturn(alias)
    val derSignature = "MEQCIFtun9Ioo-W-juCG7sOl8PPPuozb8cspsUtpu2TxnzP_AiAi1VpFNTr2eK-VX3b1DLHy8" +
        "rPm3MOpTvUH14hyNr0Gfg"
    whenever(keyStorage.sign(alias, content)).thenReturn(Base64.decode(derSignature, FLAGS))
    val signature = jwtSigner.sign(signerTemplate, content)
    assertEquals(derSignature, encodeToBase64UTF8String(signature, FLAGS))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Sign a jwt content with EC256 signer and invalid DER signature size should throw exception`() {
    val content = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
        "G4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val signerTemplate: ECP256SignerTemplate = mock()
    val alias = "test"
    whenever(signerTemplate.alias).thenReturn(alias)
    val derSignature = nextBytes(7)
    whenever(keyStorage.sign(alias, content)).thenReturn(derSignature)
    jwtSigner.sign(signerTemplate, content)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Sign a jwt content with EC256 signer and invalid first value in DER signature should throw exception`() {
    val content = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
        "G4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val signerTemplate: ECP256SignerTemplate = mock()
    val alias = "test"
    whenever(signerTemplate.alias).thenReturn(alias)
    val derSignature = nextBytes(8).apply { this[0] = nextInt(48).toByte() }
    whenever(keyStorage.sign(alias, content)).thenReturn(derSignature)
    jwtSigner.sign(signerTemplate, content)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Sign a jwt content with EC256 signer and invalid offset in DER signature should throw exception`() {
    val content = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
        "G4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val signerTemplate: ECP256SignerTemplate = mock()
    val alias = "test"
    whenever(signerTemplate.alias).thenReturn(alias)
    val derSignature = nextBytes(8).apply {
      this[0] = 48
      this[1] = 0
    }
    whenever(keyStorage.sign(alias, content)).thenReturn(derSignature)
    jwtSigner.sign(signerTemplate, content)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Sign a jwt content with EC256 signer and invalid format in DER signature should throw exception`() {
    val content = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
        "G4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
    val signerTemplate: ECP256SignerTemplate = mock()
    val alias = "test"
    whenever(signerTemplate.alias).thenReturn(alias)
    val derSignature = nextBytes(15).apply {
      this[0] = 48
      this[1] = 0x81.toByte()
      this[2] = (size - 3).toByte()
      this[4] = 3
      this[5] = 0
      this[9] = 3
      this[10] = 0
    }
    whenever(keyStorage.sign(alias, content)).thenReturn(derSignature)
    jwtSigner.sign(signerTemplate, content)
  }
}