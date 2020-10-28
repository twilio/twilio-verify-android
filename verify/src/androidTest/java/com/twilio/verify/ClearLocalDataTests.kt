/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearLocalDataTests : BaseFactorTest() {

  @Test
  fun testClearLocalDataWithFactorsShouldDeleteAndCallThen() {
    val factors = mutableListOf(factor!!)
    repeat(2) {
      createFactor().run {
        factors.add(factor!!)
      }
    }
    factors.forEach { factor ->
      assertTrue(keyStore.containsAlias(factor.keyPairAlias))
      assertTrue(encryptedSharedPreferences.contains(getFactorKey(factor)))
    }
    twilioVerify.clearLocalData {
      factors.forEach { factor ->
        assertFalse(keyStore.containsAlias(factor.keyPairAlias))
        assertFalse(encryptedSharedPreferences.contains(getFactorKey(factor)))
        assertTrue(encryptedSharedPreferences.all.isEmpty())
      }
    }
  }
}
