/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.api.APIResponses
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.UpdatePushChallengeInput
import org.junit.Assert.fail
import org.junit.Test

class ChallengeTests : BaseFactorTest() {

  @Test
  fun testUpdateChallengeWithValidDataShouldCallSuccess() {
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengeInput = UpdatePushChallengeInput(factor!!.sid, challengeSid, status)
    enqueueMockResponse(200, APIResponses.getValidPendingChallengeResponse())
    enqueueMockResponse(200, "")
    enqueueMockResponse(200, APIResponses.getValidApprovedChallengeResponse())
    idlingResource.increment()
    twilioVerify.updateChallenge(updateChallengeInput, {
      idlingResource.decrement()
    }, { e ->
      fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }
}