package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.models.UpdatePushFactorPayload
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class UpdateFactorTests : BaseFactorTest() {

  @Test
  fun testUpdatePushFactorWithValidAPIResponseShouldReturnFactor() {
    val updatePushFactorPayload = UpdatePushFactorPayload(factor!!.sid, "pushToken")
    enqueueMockResponse(200, APIResponses.updateFactorValidResponse())
    idlingResource.increment()
    twilioVerify.updateFactor(updatePushFactorPayload, {
      assertEquals(factor!!.sid, it.sid)
      idlingResource.decrement()
    }, { e ->
      fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testUpdatePushFactorWithInvalidAPIResponseCodeShouldThrowNetworkError() {
    val updatePushFactorPayload = UpdatePushFactorPayload(factor!!.sid, "pushToken")
    val expectedException = TwilioVerifyException(
        NetworkException(null, null, null),
        NetworkError
    )
    enqueueMockResponse(400, APIResponses.updateFactorValidResponse())
    idlingResource.increment()
    twilioVerify.updateFactor(updatePushFactorPayload, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testUpdatePushFactorWithInvalidAPIResponseBodyShouldThrowMapperError() {
    val updatePushFactorPayload = UpdatePushFactorPayload(factor!!.sid, "pushToken")
    val expectedException = TwilioVerifyException(
        IllegalArgumentException(null, null),
        MapperError
    )
    enqueueMockResponse(200, APIResponses.updateFactorInvalidResponse())
    idlingResource.increment()
    twilioVerify.updateFactor(updatePushFactorPayload, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }
}