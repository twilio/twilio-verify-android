/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TwilioVerifyManagerTest {

  private val factorFacade: FactorFacade = mock()
  private val twilioVerifyManager = TwilioVerifyManager(factorFacade)

  @Test
  fun `Create a factor should call success`() {
    val factorInput = PushFactorInput("name", "pushToken", "jwt")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.createFactor(eq(factorInput), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    twilioVerifyManager.createFactor(factorInput, { factor ->
      assertEquals(expectedFactor, factor)
    }, {
      fail()
    })
  }

  @Test
  fun `Error creating a factor should call error`() {
    val factorInput = PushFactorInput("name", "pushToken", "jwt")
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.createFactor(eq(factorInput), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    twilioVerifyManager.createFactor(factorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Verify a factor should call success`() {
    val verifyFactorInput = VerifyPushFactorInput("sid", "verificationCode")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.verifyFactor(eq(verifyFactorInput), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    twilioVerifyManager.verifyFactor(verifyFactorInput, { factor ->
      assertEquals(expectedFactor, factor)
    }, {
      fail()
    })
  }

  @Test
  fun `Error verifying a factor should call error`() {
    val verifyFactorInput = VerifyPushFactorInput("sid", "verificationCode")
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.verifyFactor(eq(verifyFactorInput), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    twilioVerifyManager.verifyFactor(verifyFactorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }
}