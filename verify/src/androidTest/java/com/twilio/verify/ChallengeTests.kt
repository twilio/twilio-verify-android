/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.api.APIResponses
import com.twilio.verify.api.signatureFieldsHeader
import com.twilio.verify.domain.challenge.signatureFieldsHeaderSeparator
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.UpdatePushChallengePayload
import org.json.JSONObject
import org.junit.Assert.fail
import org.junit.Test

class ChallengeTests : BaseFactorTest() {

  @Test
  fun testUpdateChallengeWithValidDataShouldCallSuccess() {
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factor!!.sid, challengeSid, status)
    val response = JSONObject(APIResponses.getValidPendingChallengeResponse())
    val headers = response.keys()
      .asSequence()
      .toList()
      .joinToString(
        signatureFieldsHeaderSeparator
      )
    enqueueMockResponse(
      200, response.toString(),
      headers = mapOf(
        signatureFieldsHeader to listOf(headers)
      )
    )
    enqueueMockResponse(200, "")
    enqueueMockResponse(200, APIResponses.getValidApprovedChallengeResponse())
    idlingResource.increment()
    twilioVerify.updateChallenge(
      updateChallengePayload,
      {
        idlingResource.decrement()
      },
      { e ->
        fail(e.message)
        idlingResource.decrement()
      }
    )
    idlingResource.waitForResource()
  }
}
