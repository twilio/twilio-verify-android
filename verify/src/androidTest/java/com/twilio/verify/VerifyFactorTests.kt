package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.VerifyPushFactorPayload
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/*
 * Copyright (c) 2020, Twilio Inc.
 */
class VerifyFactorTests : BaseFactorTest() {

  @Test
  fun testVerifyFactorWithValidAPIResponseShouldReturnFactor() {
    val verifyPayload = VerifyPushFactorPayload(factor!!.sid)
    enqueueMockResponse(200, APIResponses.verifyValidFactorResponse())
    idlingResource.increment()
    twilioVerify.verifyFactor(
      verifyPayload,
      {
        assertEquals(factor!!.sid, it.sid)
        idlingResource.decrement()
      },
      { e ->
        fail(e.message)
        idlingResource.decrement()
      }
    )
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithInvalidAPIResponseCodeShouldThrowNetworkError() {
    val verifyFactorPayload = VerifyPushFactorPayload(factor!!.sid)
    val expectedException = TwilioVerifyException(
      NetworkException(null, null, null),
      NetworkError
    )
    enqueueMockResponse(400, APIResponses.verifyValidFactorResponse())
    idlingResource.increment()
    twilioVerify.verifyFactor(
      verifyFactorPayload,
      {
        fail()
        idlingResource.decrement()
      },
      { exception ->
        assertEquals(expectedException.message, exception.message)
        idlingResource.decrement()
      }
    )
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithInvalidAPIResponseBodyShouldThrowMapperError() {
    val verifyFactorPayload = VerifyPushFactorPayload(factor!!.sid)
    val expectedException = TwilioVerifyException(
      IllegalArgumentException(null, null),
      MapperError
    )
    enqueueMockResponse(200, APIResponses.verifyInvalidFactorResponse())
    idlingResource.increment()
    twilioVerify.verifyFactor(
      verifyFactorPayload,
      {
        fail()
        idlingResource.decrement()
      },
      { exception ->
        assertEquals(expectedException.message, exception.message)
        idlingResource.decrement()
      }
    )
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithCreatedFactorAndValidAPIResponseShouldReturnFactor() {
    createFactor {
      factor = it as PushFactor
      val verifyFactorPayload = VerifyPushFactorPayload(factor!!.sid)
      enqueueMockResponse(200, APIResponses.verifyValidFactorResponse())
      idlingResource.increment()
      twilioVerify.verifyFactor(
        verifyFactorPayload,
        { verifiedFactor ->
          assertEquals(factor!!.sid, verifiedFactor.sid)
          idlingResource.decrement()
        },
        { e ->
          e.printStackTrace()
          fail(e.message)
          idlingResource.decrement()
        }
      )
    }
    idlingResource.waitForResource()
  }
}
