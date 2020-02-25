/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.data.StorageException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactorFacadeTest {

  private val pushFactory: PushFactory = mock()
  private val factorProvider: FactorProvider = mock()
  private val factorFacade = FactorFacade(pushFactory, factorProvider)

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

  @Test
  fun `Verify a factor should call success`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val verifyFactorInput = VerifyPushFactorInput(sid, verificationCode)
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(
          pushFactory.verify(
              eq(sid), eq(verificationCode), capture(), any()
          )
      ).then {
        firstValue.invoke(expectedFactor)
      }
    }
    factorFacade.verifyFactor(verifyFactorInput, { factor ->
      assertEquals(expectedFactor, factor)
    }, {
      fail()
    })
  }

  @Test
  fun `Error verifying a factor should call error`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val verifyFactorInput = VerifyPushFactorInput(sid, verificationCode)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
          pushFactory.verify(
              eq(sid), eq(verificationCode), any(), capture()
          )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    factorFacade.verifyFactor(verifyFactorInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
    })
  }

  @Test
  fun `Get a factor with factor already stored should return expected factor`() {
    val sid = "sid"
    val expectedFactor: Factor = mock()
    whenever(factorProvider.get(sid)).thenReturn(expectedFactor)
    factorFacade.getFactor(sid, { factor ->
      assertEquals(expectedFactor, factor)
    }, {
      fail()
    })
  }

  @Test
  fun `Get a factor with no factor stored should return error`() {
    val sid = "sid"
    whenever(factorProvider.get(sid)).thenReturn(null)
    factorFacade.getFactor(sid, {
      fail()
    }, { exception ->
      assertTrue(exception.cause is StorageException)
      assertEquals(StorageError.message, exception.localizedMessage)
    })
  }

  @Test
  fun `Get a factor with exception should return error`() {
    val sid = "sid"
    val expectedException: TwilioVerifyException = mock()
    given(factorProvider.get(sid)).willAnswer {
      throw expectedException
    }
    factorFacade.getFactor(sid, {
      fail()
    }, { exception ->
      assertEquals(expectedException, exception)
    })
  }
}