package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.data.provider
import com.twilio.verify.domain.factor.FactorMapper
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType.PUSH
import com.twilio.verify.models.PushFactorPayload
import com.twilio.verify.networking.NetworkException
import org.junit.After
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

  internal var keyPairAlias: String? = null

  @After
  override fun tearDown() {
    keyPairAlias?.let { alias ->
      keyStore.deleteEntry(alias)
    }
    super.tearDown()
  }

  @Test
  fun testCreateFactorWithValidAccessTokenAndValidAPIResponseShouldReturnFactor() {
    val friendlyName = "friendlyName"
    val accessToken = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorPayload =
      PushFactorPayload(friendlyName, "serviceSid", "identity", "pushToken", accessToken)
    enqueueMockResponse(200, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorPayload, {
      assertEquals(friendlyName, it.friendlyName)
      assertTrue(it is PushFactor)
      assertEquals(PUSH, it.type)
      keyPairAlias = (it as PushFactor).keyPairAlias
      assertNotNull(keyPairAlias)
      checkFactorWasStored(it)
      checkKeyPairWasCreated(it)
      idlingResource.decrement()
    }, {
      fail(it.message)
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
    assertEquals(factor.identity, storedFactor.identity)
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
  fun testCreateFactorWithValidAccessTokenAndInvalidAPIResponseCodeShouldThrowNetworkError() {
    val friendlyName = "friendlyName"
    val accessToken = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorPayload =
      PushFactorPayload(friendlyName, "serviceSid", "identity", "pushToken", accessToken)
    val expectedException = TwilioVerifyException(
        NetworkException(null, null, null),
        NetworkError
    )
    enqueueMockResponse(400, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorPayload, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      assertTrue(
          keyStore.aliases()
              .toList()
              .none { !it.startsWith(context.packageName) })
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testCreateFactorWithValidAccessTokenAndInvalidAPIResponseBodyShouldThrowMapperError() {
    val friendlyName = "friendlyName"
    val accessToken = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorPayload =
      PushFactorPayload(friendlyName, "serviceSid", "identity", "pushToken", accessToken)
    val expectedException = TwilioVerifyException(
        IllegalArgumentException(null, null),
        MapperError
    )
    enqueueMockResponse(200, APIResponses.createInvalidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorPayload, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }
}