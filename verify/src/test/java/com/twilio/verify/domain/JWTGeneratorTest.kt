/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import android.util.Base64
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.factor.DEFAULT_ALG
import com.twilio.verify.networking.subKey
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class JWTGeneratorTest {

  private val keyStorage: KeyStorage = mock()
  private val jwtGenerator = JWTGenerator(keyStorage)

  @Test
  fun `Generate JWT s hould sign message`() {
    val alias = "alias"
    val header = JSONObject().apply {
      put(ALGORITHM_KEY, DEFAULT_ALG)
    }
    val payload = JSONObject().apply {
      put(subKey, "sub")
    }
    val message = "${Base64.encodeToString(
        header.toString()
            .toByteArray(), FLAGS
    )}.${Base64.encodeToString(
        payload.toString()
            .toByteArray(), FLAGS
    )}"
    val signature = "MEQCIBte4t"
    whenever(keyStorage.sign(alias, message, FLAGS)).thenReturn(signature)
    val jwt = jwtGenerator.generateJWT(alias, header, payload)
    assertEquals("$message.$signature", jwt)
  }
}