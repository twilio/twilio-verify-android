/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.api.SIGNATURE_FIELDS_HEADER
import com.twilio.verify.domain.challenge.SIGNATURE_FIELDS_HEADER_SEPARATOR
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.UpdatePushChallengePayload
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ChallengeTests : BaseFactorTest() {
  @Test
  fun testUpdateChallengeWithValidDataShouldCallSuccess() {
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factor!!.sid, challengeSid, status)
    val response = JSONObject(APIResponses.getValidPendingChallengeResponse())
    val headers =
      response
        .keys()
        .asSequence()
        .toList()
        .joinToString(
          SIGNATURE_FIELDS_HEADER_SEPARATOR,
        )
    enqueueMockResponse(
      200,
      response.toString(),
      headers =
        mapOf(
          SIGNATURE_FIELDS_HEADER to listOf(headers),
        ),
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
      },
    )
    idlingResource.waitForResource()
  }

  @Test
  fun testUpdateChallengeWithInvalidFactorSidShouldThrowError() {
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(" ", challengeSid, status)
    val expectedException = TwilioVerifyException(IllegalArgumentException("Empty factor sid"), InputError)
    idlingResource.increment()
    twilioVerify.updateChallenge(
      updateChallengePayload,
      {
        fail()
        idlingResource.decrement()
      },
      { exception ->
        assertEquals(expectedException.message, exception.message)
        assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
        idlingResource.decrement()
      },
    )
    idlingResource.waitForResource()
  }

  @Test
  fun testUpdateChallengeWithInvalidChallengeSidShouldThrowError() {
    val challengeSid = " "
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factor!!.sid, challengeSid, status)
    val expectedException = TwilioVerifyException(IllegalArgumentException("Empty challenge sid"), InputError)
    idlingResource.increment()
    twilioVerify.updateChallenge(
      updateChallengePayload,
      {
        fail()
        idlingResource.decrement()
      },
      { exception ->
        assertEquals(expectedException.message, exception.message)
        assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
        idlingResource.decrement()
      },
    )
    idlingResource.waitForResource()
  }
}
