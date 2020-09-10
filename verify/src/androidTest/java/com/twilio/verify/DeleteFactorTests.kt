/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DeleteFactorTests : BaseFactorTest() {

  @Test
  fun testDeleteFactorWithValidFactorShouldCallSuccess() {
    assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
    assertTrue(sharedPreferences.contains(factor!!.sid))
    enqueueMockResponse(200)
    idlingResource.increment()
    twilioVerify.deleteFactor(
      factor!!.sid,
      {
        assertFalse(keyStore.containsAlias(factor!!.keyPairAlias))
        assertFalse(sharedPreferences.contains(factor!!.sid))
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
  fun testDeleteFactorWithInvalidAPIResponseShouldCallError() {
    assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
    assertTrue(sharedPreferences.contains(factor!!.sid))
    val expectedException = TwilioVerifyException(
      NetworkException(null, null, null),
      NetworkError
    )
    enqueueMockResponse(400)
    idlingResource.increment()
    twilioVerify.deleteFactor(
      factor!!.sid,
      {
        fail()
        idlingResource.decrement()
      },
      { exception ->
        assertEquals(expectedException.message, exception.message)
        assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
        idlingResource.decrement()
      }
    )
    idlingResource.waitForResource()
  }
}
