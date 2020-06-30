/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.api.APIResponses
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorPayload
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before

open class BaseFactorTest : BaseServerTest() {

  internal var factor: PushFactor? = null

  @Before
  override fun before() {
    super.before()
    createFactor()
  }

  @After
  override fun tearDown() {
    factor?.keyPairAlias?.let { alias ->
      keyStore.deleteEntry(alias)
    }
    super.tearDown()
  }

  protected fun createFactor(onSuccess: (Factor) -> Unit = {}) {
    val friendlyName = "friendlyName"
    val jwe = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorPayload =
      PushFactorPayload(friendlyName, "serviceSid", "identity", "pushToken", jwe)
    enqueueMockResponse(200, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorPayload, {
      factor = it as PushFactor
      onSuccess(it)
      idlingResource.decrement()
    }, { e ->
      fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }
}