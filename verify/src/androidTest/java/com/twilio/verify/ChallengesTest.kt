/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.api.APIResponses
import com.twilio.verify.models.ChallengeListPayload
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test

class ChallengesTest : BaseFactorTest() {

  @Test
  fun testGetAllChallengesWithValidDataShouldCallSuccess() {
    val response = JSONObject(APIResponses.getAllChallengesResponse())
    enqueueMockResponse(200, response.toString())
    val challengeListPayload = ChallengeListPayload(factor!!.sid, 20)
    idlingResource.increment()
    twilioVerify.getAllChallenges(
      challengeListPayload,
      {
        assertEquals(2, it.challenges.size)
        assertEquals(2, it.metadata.pageSize)
        assertEquals(0, it.metadata.page)
        assertNotNull(it.metadata.previousPageToken)
        assertNotNull(it.metadata.nextPageToken)
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
