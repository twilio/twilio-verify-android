/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorFacadeTest {

  private val pushFactory: PushFactory = mock()
  private val factorFacade = FactorFacade(pushFactory)

  @Test
  fun `Create a factor should call success`() {
    val factorInput = PushFactorInput("name", "pushToken", "jwt")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(
          pushFactory.create(
              eq(factorInput.jwt), eq(factorInput.friendlyName), eq(factorInput.pushToken),
              capture(), any()
          )
      ).then {
        firstValue.invoke(expectedFactor)
      }
    }
    factorFacade.createFactor(factorInput, { factor ->
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
      whenever(
          pushFactory.create(
              eq(factorInput.jwt), eq(factorInput.friendlyName), eq(factorInput.pushToken),
              any(), capture()
          )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    factorFacade.createFactor(factorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }
}