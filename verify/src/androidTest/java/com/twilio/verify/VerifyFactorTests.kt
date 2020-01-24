package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class VerifyFactorTests : BaseServerTest() {

  override fun before() {
    createFactor()
    super.before()
  }

  private fun createFactor() {
    super.before()
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
      keyPairAlias = (it as PushFactor).keyPairAlias
    }, {
      fail()
    })
    mockWebServer.shutdown()
  }

  @Test
  fun testVerifyFactorWithValidAPIResponseShouldReturnFactor() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    enqueueMockResponse(200, APIResponses.verifyValidFactorResponse())
    twilioVerify.verifyFactor(verifyInput, {
      assertEquals(sid, it.sid)
    }, {
      fail()
    })
  }

  @Test
  fun testVerifyFactorWithInvalidAPIResponseCodeShouldThrowNetworkError() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    val expectedException = TwilioVerifyException(
        NetworkException(null, null),
        NetworkError
    )
    enqueueMockResponse(400, APIResponses.verifyValidFactorResponse())
    twilioVerify.verifyFactor(verifyInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
  }

  @Test
  fun testVerifyFactorWithInvalidAPIResponseBodyShouldThrowMapperError() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    val expectedException = TwilioVerifyException(
        IllegalArgumentException(null, null),
        MapperError
    )
    enqueueMockResponse(200, APIResponses.verifyInvalidFactorResponse())
    twilioVerify.verifyFactor(verifyInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
  }
}