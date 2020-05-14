package com.twilio.verify.domain.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.api.ServiceAPIClient
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.sidKey
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.service.models.FactorService
import com.twilio.verify.models.Factor
import org.json.JSONObject
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
class ServiceRepositoryTest {

  private val apiClient: ServiceAPIClient = mock()
  private val serviceMapper: ServiceMapper = mock()
  private val serviceRepository = ServiceRepository(apiClient, serviceMapper)

  @Test
  fun `Get service with valid response should return a service`() {
    val serviceSid = "sid123"
    val service: FactorService = mock()
    val factor: Factor = mock()
    val response = JSONObject().apply {
      put(sidKey, serviceSid)
      put(accountSidKey, "accountSid")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(serviceSid), any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(serviceMapper.fromApi(response)).thenReturn(service)
    serviceRepository.get(serviceSid, factor, {
      assertEquals(service, it)
    }, {
      fail()
    })
  }

  @Test
  fun `No response from API getting a service should call error`() {
    val serviceSid = "sid123"
    val factor: Factor = mock()
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(apiClient.get(eq(serviceSid), any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    serviceRepository.get(serviceSid, factor, { fail() }, { exception ->
      assertEquals(expectedException, exception)
    })
  }

  @Test
  fun `Error from mapper getting a service should call error`() {
    val serviceSid = "sid123"
    val factor: Factor = mock()
    val response = JSONObject().apply {
      put(sidKey, serviceSid)
      put(accountSidKey, "accountSid")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(serviceSid), any(), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(serviceMapper.fromApi(response)).thenThrow(expectedException)
    serviceRepository.get(serviceSid, factor, { fail() }, { exception ->
      assertEquals(expectedException, exception)
    })
  }
}