/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data.jwt

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.template.ECP256SignerTemplate
import com.twilio.verify.data.encodeToBase64UTF8String
import com.twilio.verify.domain.factor.DEFAULT_ALG
import com.twilio.verify.networking.subKey
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random.Default.nextBytes

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class JwtGeneratorTest {

  private val jwtSigner: JwtSigner = mock()
  private val jwtGenerator = JwtGenerator(jwtSigner)

  @Test
  fun `Generate JWT should sign message`() {
    val signerTemplate: ECP256SignerTemplate = mock()
    val header = JSONObject().apply {
      put(ALGORITHM_KEY, DEFAULT_ALG)
      put(typeKey, jwtType)
    }
    val payload = JSONObject().apply {
      put(subKey, "sub")
    }
    val message = "${encodeToBase64UTF8String(
        header.toString()
            .toByteArray(), FLAGS
    )}.${encodeToBase64UTF8String(
        payload.toString()
            .toByteArray(), FLAGS
    )}"
    val signature = nextBytes(10)
    whenever(jwtSigner.sign(signerTemplate, message)).thenReturn(signature)
    val jwt = jwtGenerator.generateJWT(signerTemplate, header, payload)
    assertEquals("$message.${encodeToBase64UTF8String(signature, FLAGS)}", jwt)
  }
}