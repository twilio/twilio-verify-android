/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.dateHeaderKey
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
    assertTrue(encryptedSharedPreferences.contains(getFactorKey(factor!!)))
    enqueueMockResponse(200)
    idlingResource.increment()
    twilioVerify.deleteFactor(
      factor!!.sid,
      {
        assertFalse(keyStore.containsAlias(factor!!.keyPairAlias))
        assertFalse(encryptedSharedPreferences.contains(getFactorKey(factor!!)))
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
  fun testDeleteFactorWithNoExistingFactorShouldCallSuccess() {
    assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
    assertTrue(sharedPreferences.contains(factor!!.sid))
    enqueueMockResponse(401, headers = mapOf(dateHeaderKey to listOf("Tue, 21 Jul 2020 17:07:32 GMT")))
    enqueueMockResponse(401)
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
    assertTrue(encryptedSharedPreferences.contains(getFactorKey(factor!!)))
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
