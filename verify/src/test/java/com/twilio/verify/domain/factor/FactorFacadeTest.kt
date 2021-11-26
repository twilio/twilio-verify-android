/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.StorageError
import com.twilio.verify.data.StorageException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorPayload
import com.twilio.verify.models.UpdatePushFactorPayload
import com.twilio.verify.models.VerifyPushFactorPayload
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
  private val idlingResource = IdlingResource()

  @Test
  fun `Create a factor should call success`() {
    val factorPayload =
      PushFactorPayload("friendly name", "serviceSid", "identity", "pushToken", "accessToken")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(
        pushFactory.create(
          eq(factorPayload.accessToken), eq(factorPayload.friendlyName), eq(factorPayload.serviceSid),
          eq(factorPayload.identity), eq(factorPayload.pushToken), capture(), any()
        )
      ).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    factorFacade.createFactor(
      factorPayload,
      { factor ->
        assertEquals(expectedFactor, factor)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error creating a factor should call error`() {
    val factorPayload =
      PushFactorPayload("friendly name", "serviceSid", "identity", "pushToken", "accessToken")
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
        pushFactory.create(
          eq(factorPayload.accessToken), eq(factorPayload.friendlyName), eq(factorPayload.serviceSid),
          eq(factorPayload.identity), eq(factorPayload.pushToken), any(), capture()
        )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    factorFacade.createFactor(
      factorPayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify a factor should call success`() {
    val sid = "sid"
    val verifyFactorPayload = VerifyPushFactorPayload(sid)
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(
        pushFactory.verify(
          eq(sid), capture(), any()
        )
      ).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    factorFacade.verifyFactor(
      verifyFactorPayload,
      { factor ->
        assertEquals(expectedFactor, factor)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error verifying a factor should call error`() {
    val sid = "sid"
    val verifyFactorPayload = VerifyPushFactorPayload(sid)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
        pushFactory.verify(
          eq(sid), any(), capture()
        )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    factorFacade.verifyFactor(
      verifyFactorPayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update a factor should call success`() {
    val sid = "sid"
    val pushToken = "pushToken"
    val updatePushFactorPayload = UpdatePushFactorPayload(sid, pushToken)
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(
        pushFactory.update(
          eq(sid), eq(pushToken), capture(), any()
        )
      ).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    factorFacade.updateFactor(
      updatePushFactorPayload,
      { factor ->
        assertEquals(expectedFactor, factor)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error updating a factor should call error`() {
    val sid = "sid"
    val pushToken = "pushToken"
    val updatePushFactorPayload = UpdatePushFactorPayload(sid, pushToken)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
        pushFactory.update(
          eq(sid), eq(pushToken), any(), capture()
        )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    factorFacade.updateFactor(
      updatePushFactorPayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a factor with factor already stored should return expected factor`() {
    val sid = "sid"
    val expectedFactor: Factor = mock()
    whenever(factorProvider.get(sid)).thenReturn(expectedFactor)
    idlingResource.startOperation()
    factorFacade.getFactor(
      sid,
      { factor ->
        assertEquals(expectedFactor, factor)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a factor with no factor stored should return error`() {
    val sid = "sid"
    whenever(factorProvider.get(sid)).thenReturn(null)
    idlingResource.startOperation()
    factorFacade.getFactor(
      sid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is StorageException)
        assertEquals(StorageError.message, exception.localizedMessage)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a factor with exception should return error`() {
    val sid = "sid"
    val expectedException: TwilioVerifyException = mock()
    given(factorProvider.get(sid)).willAnswer {
      throw expectedException
    }
    idlingResource.startOperation()
    factorFacade.getFactor(
      sid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a factor by service sid with factor already stored should return expected factor`() {
    val factorServiceSid = "sid"
    val expectedFactor: Factor = mock() {
      on { serviceSid } doReturn factorServiceSid
    }
    whenever(factorProvider.getAll()).thenReturn(listOf(expectedFactor))
    idlingResource.startOperation()
    factorFacade.getFactorByServiceSid(
      factorServiceSid,
      { factor ->
        assertEquals(expectedFactor, factor)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a factor by service sid with no factor stored should return error`() {
    val factorServiceSid = "sid"
    whenever(factorProvider.getAll()).thenReturn(listOf(mock()))
    idlingResource.startOperation()
    factorFacade.getFactorByServiceSid(
      factorServiceSid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is StorageException)
        assertEquals(StorageError.message, exception.localizedMessage)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get a factor by service sid with exception should return error`() {
    val expectedException: TwilioVerifyException = mock()
    given(factorProvider.getAll()).willAnswer {
      throw expectedException
    }
    idlingResource.startOperation()
    factorFacade.getFactorByServiceSid(
      "sid",
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all factors should return factor list`() {
    val expectedFactorList: List<Factor> = mock()
    whenever(factorProvider.getAll()).thenReturn(expectedFactorList)
    idlingResource.startOperation()
    factorFacade.getAllFactors(
      { factorList ->
        assertEquals(expectedFactorList, factorList)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all factors with exception should return error`() {
    val expectedException: TwilioVerifyException = mock()
    given(factorProvider.getAll()).willAnswer {
      throw expectedException
    }
    idlingResource.startOperation()
    factorFacade.getAllFactors(
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Delete a factor should call success`() {
    val factorSid = "factorSid"
    argumentCaptor<() -> Unit>().apply {
      whenever(
        pushFactory.delete(
          eq(factorSid), capture(), any()
        )
      ).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    factorFacade.deleteFactor(
      factorSid,
      {
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Delete a factor should call error`() {
    val factorSid = "factorSid"
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
        pushFactory.delete(
          eq(factorSid), any(), capture()
        )
      ).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    factorFacade.deleteFactor(
      factorSid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Clear local data should delete all factors from push factory`() {
    argumentCaptor<() -> Unit>().apply {
      whenever(pushFactory.deleteAllFactors(capture())).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    factorFacade.clearLocalStorage {
      verify(pushFactory).deleteAllFactors(any())
      idlingResource.operationFinished()
    }
    idlingResource.waitForIdle()
  }

  @Test
  fun `Clear local data when push factory throws error should clear storage`() {
    whenever(pushFactory.deleteAllFactors(any())).thenThrow(RuntimeException())
    idlingResource.startOperation()
    factorFacade.clearLocalStorage {
      verify(factorProvider).clearLocalStorage()
      idlingResource.operationFinished()
    }
    idlingResource.waitForIdle()
  }
}
