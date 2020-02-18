package com.twilio.verify

import androidx.test.espresso.idling.CountingIdlingResource
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.data.provider
import com.twilio.verify.domain.factor.FactorMapper
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.KeyStore

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class CreateFactorTests : BaseServerTest() {

  private val idlingResource = CountingIdlingResource("VerifyFactorTests")

  @Test
  fun testCreateFactorWithValidJWTAndValidAPIResponseShouldReturnFactor() {
    val friendlyName = "friendlyName"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput(friendlyName, "pushToken", jwt)
    enqueueMockResponse(200, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorInput, {
      assertEquals(friendlyName, it.friendlyName)
      assertTrue(it is PushFactor)
      assertEquals(Push, it.type)
      keyPairAlias = (it as PushFactor).keyPairAlias
      assertNotNull(keyPairAlias)
      checkFactorWasStored(it)
      checkKeyPairWasCreated(it)
      idlingResource.decrement()
    }, {
      fail()
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  private fun checkFactorWasStored(factor: Factor) {
    val storedFactorSid = sharedPreferences.getString(factor.sid, null)
    assertNotNull(storedFactorSid)
    val storedFactor = FactorMapper().fromStorage(storedFactorSid!!)
    assertEquals(factor.type, storedFactor.type)
    assertEquals(factor.friendlyName, storedFactor.friendlyName)
    assertEquals(factor.status, storedFactor.status)
    assertEquals(factor.entitySid, storedFactor.entitySid)
    assertEquals(factor.entityId, storedFactor.entityId)
    assertEquals(factor.accountSid, storedFactor.accountSid)
    assertEquals(factor.serviceSid, storedFactor.serviceSid)
  }

  private fun checkKeyPairWasCreated(factor: Factor) {
    val alias = (factor as PushFactor).keyPairAlias
    val keyStore = KeyStore.getInstance(provider)
    keyStore.load(null)
    val entry = keyStore.getEntry(alias, null)
    val privateKey = (entry as KeyStore.PrivateKeyEntry).privateKey
    val publicKey = keyStore.getCertificate(alias)
        .publicKey
    assertNotNull(privateKey)
    assertNotNull(publicKey)
  }

  @Test
  fun testCreateFactorWithInvalidJWTShouldThrowInputError() {
    val friendlyName = "friendlyName"
    val jwt = "jwt"
    val factorInput = PushFactorInput(friendlyName, "pushToken", jwt)
    val expectedException = TwilioVerifyException(
        IllegalArgumentException(),
        InputError
    )
    enqueueMockResponse(200, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorInput, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testCreateFactorWithValidJWTAndInvalidAPIResponseCodeShouldThrowNetworkError() {
    val friendlyName = "friendlyName"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput(friendlyName, "pushToken", jwt)
    val expectedException = TwilioVerifyException(
        NetworkException(null, null),
        NetworkError
    )
    enqueueMockResponse(400, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorInput, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      assertFalse(keyStore.aliases().hasMoreElements())
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testCreateFactorWithValidJWTAndInvalidAPIResponseBodyShouldThrowMapperError() {
    val friendlyName = "friendlyName"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput(friendlyName, "pushToken", jwt)
    val expectedException = TwilioVerifyException(
        IllegalArgumentException(null, null),
        MapperError
    )
    enqueueMockResponse(200, APIResponses.createInvalidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorInput, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }
}