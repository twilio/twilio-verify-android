/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.domain.factor.models.PushFactor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearLocalDataTests : BaseFactorTest() {

  @Test
  fun testClearLocalDataWithFactorShouldDeleteAndCallThen() {
    assertTrue(keyStore.containsAlias(factor!!.keyPairAlias))
    assertTrue(encryptedSharedPreferences.contains(getFactorKey(factor!!)))
    idlingResource.increment()
    twilioVerify.clearLocalStorage {
      assertTrue(encryptedSharedPreferences.all.isEmpty())
      assertFalse(keyStore.containsAlias(factor!!.keyPairAlias))
      assertFalse(encryptedSharedPreferences.contains(getFactorKey(factor!!)))
      idlingResource.decrement()
    }
    idlingResource.waitForResource()
  }

  @Test
  fun testClearLocalDataWithFactorsShouldDeleteAndCallThen() {
    val factors = mutableListOf<PushFactor>().apply {
      repeat(2) {
        createFactor(sid = "factorSid$it") { factor ->
          add(factor as PushFactor)
        }
      }
    }
    factors.forEach { factor ->
      assertTrue(keyStore.containsAlias(factor.keyPairAlias))
      assertTrue(encryptedSharedPreferences.contains(getFactorKey(factor)))
    }
    idlingResource.increment()
    twilioVerify.clearLocalStorage {
      assertTrue(encryptedSharedPreferences.all.isEmpty())
      factors.forEach { factor ->
        assertFalse(keyStore.containsAlias(factor.keyPairAlias))
        assertFalse(encryptedSharedPreferences.contains(getFactorKey(factor)))
      }
      idlingResource.decrement()
    }
    idlingResource.waitForResource()
  }
}
