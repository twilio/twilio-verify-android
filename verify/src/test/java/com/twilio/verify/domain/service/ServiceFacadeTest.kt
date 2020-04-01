package com.twilio.verify.domain.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.models.Service
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ServiceFacadeTest {

  private val serviceProvider: ServiceProvider = mock()
  private val serviceFacade = ServiceFacade(serviceProvider)
  private val idlingResource = IdlingResource()

  @Test
  fun `Get a service should call success`() {
    val serviceSid = "serviceSid"
    val expectedService: Service = mock()
    argumentCaptor<(Service) -> Unit>().apply {
      whenever(
          serviceProvider.get(
              eq(serviceSid), capture(), any()
          )
      ).then {
        firstValue.invoke(expectedService)
      }
    }
    idlingResource.startOperation()
    serviceFacade.getService(serviceSid, { service ->
      assertEquals(expectedService, service)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting a service should call error`() {
    val serviceSid = "serviceSid"
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
          serviceProvider.get(
              eq(serviceSid), any(), capture()
          )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    serviceFacade.getService(serviceSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

}