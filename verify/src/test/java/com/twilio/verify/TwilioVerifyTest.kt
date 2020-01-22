/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.entitySidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.domain.factor.statusKey
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [TestKeystore::class])
class TwilioVerifyTest {

  private val networkProvider: NetworkProvider = mock()
  private val authorization = Authorization("accountSid", "authToken")
  private val twilioVerify =
    TwilioVerify.Builder(ApplicationProvider.getApplicationContext(), authorization)
        .networkProvider(networkProvider)
        .build()

  @Test
  fun `Create a factor should call success`() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(entitySidKey, "entitySid123")
        .put(statusKey, FactorStatus.Unverified.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(jsonObject.toString())
      }
    }
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput("friendly name", "pushToken", jwt)
    twilioVerify.createFactor(factorInput, { factor ->
      assertEquals(jsonObject.getString(sidKey), factor.sid)
    }, { exception ->
      fail(exception.message)
    })
  }
}

@Implements(com.twilio.verify.data.AndroidKeyStore::class)
class TestKeystore {
  private val keys = mutableMapOf<String, KeyPair>()
  @Implementation
  fun createKeyPair(alias: String): KeyPair {
    val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("EC")
    val random: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
    keyGen.initialize(256, random)
    return keyGen.generateKeyPair()
        .apply { keys[alias] = this }
  }

  @Implementation
  fun sign(
    alias: String,
    message: String
  ): ByteArray {
    val dsa = Signature.getInstance("SHA256withRSA")
    dsa.initSign(keys[alias]?.private)
    dsa.update(message.toByteArray())
    return dsa.sign()
  }
}