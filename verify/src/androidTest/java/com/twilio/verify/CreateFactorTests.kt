package com.twilio.verify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.verify.TwilioVerify.Builder
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.data.Storage
import com.twilio.verify.data.provider
import com.twilio.verify.domain.factor.FactorMapper
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.sharedPreferencesName
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.KeyStore

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class CreateFactorTests : BaseServerTest() {

  private lateinit var twilioVerify: TwilioVerify

  override fun before() {
    super.before()
    val context: Context = InstrumentationRegistry.getInstrumentation()
        .targetContext
    val authorization = Authorization("accountSid", "authToken")
    twilioVerify = Builder(context, authorization)
        .networkProvider(NetworkAdapter(httpsURLConnection))
        .build()
  }

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
    twilioVerify.createFactor(factorInput, {
      assertEquals(friendlyName, it.friendlyName)
      assertTrue(it is PushFactor)
      assertEquals(Push, it.type)
      assertNotNull((it as PushFactor).keyPairAlias)
      checkFactorWasStored(it)
      checkKeyPairWasCreated(it)
    }, {
      fail()
    })
  }

  private fun checkFactorWasStored(factor: Factor) {
    val sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    val storage = Storage(sharedPreferences)
    val storedFactorSid = storage.get(factor.sid)
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
    twilioVerify.createFactor(factorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
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
    twilioVerify.createFactor(factorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
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
    twilioVerify.createFactor(factorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
  }
}