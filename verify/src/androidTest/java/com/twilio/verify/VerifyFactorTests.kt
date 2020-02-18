package com.twilio.verify

import androidx.test.espresso.idling.CountingIdlingResource
import com.twilio.verify.TwilioVerify.Builder
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */
class VerifyFactorTests : BaseServerTest() {

  private val idlingResource = CountingIdlingResource("VerifyFactorTests")

  override fun before() {
    createFactor()
    super.before()
  }

  private fun createFactor() {
    super.before()
    val friendlyName = "friendlyName"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput(friendlyName, "pushToken", jwt)
    enqueueMockResponse(200, APIResponses.createValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorInput, {
      keyPairAlias = (it as PushFactor).keyPairAlias
      idlingResource.decrement()
    }, { e ->
      fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithValidAPIResponseShouldReturnFactor() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    enqueueMockResponse(200, APIResponses.verifyValidFactorResponse())
    idlingResource.increment()
    twilioVerify.verifyFactor(verifyInput, {
      assertEquals(sid, it.sid)
      idlingResource.decrement()
    }, { e ->
      fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithInvalidAPIResponseCodeShouldThrowNetworkError() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    val expectedException = TwilioVerifyException(
        NetworkException(null, null),
        NetworkError
    )
    enqueueMockResponse(400, APIResponses.verifyValidFactorResponse())
    idlingResource.increment()
    twilioVerify.verifyFactor(verifyInput, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithInvalidAPIResponseBodyShouldThrowMapperError() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    val expectedException = TwilioVerifyException(
        IllegalArgumentException(null, null),
        MapperError
    )
    enqueueMockResponse(200, APIResponses.verifyInvalidFactorResponse())
    idlingResource.increment()
    twilioVerify.verifyFactor(verifyInput, {
      fail()
      idlingResource.decrement()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }

  @Test
  fun testVerifyFactorWithCreatedFactorAndValidAPIResponseShouldReturnFactor() {
    tearDown()
    super.before()
    val sid = "sid"
    val friendlyName = "friendlyName"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val factorInput = PushFactorInput(friendlyName, "pushToken", jwt)
    enqueueMockResponse(200, APIResponses.createValidFactorResponse())
    enqueueMockResponse(200, APIResponses.verifyValidFactorResponse())
    idlingResource.increment()
    twilioVerify.createFactor(factorInput, {
      keyPairAlias = (it as PushFactor).keyPairAlias
      idlingResource.decrement()
      val verifyInput = VerifyPushFactorInput(sid)
      idlingResource.increment()
      httpsURLConnection =
        URL(mockWebServer.url("/").toString()).openConnection() as HttpURLConnection
      twilioVerify = Builder(context, authorization)
          .networkProvider(NetworkAdapter(httpsURLConnection))
          .build()
      twilioVerify.verifyFactor(verifyInput, { createdFactor ->
        assertEquals(sid, createdFactor.sid)
        idlingResource.decrement()
      }, { e ->
        e.printStackTrace()
        fail(e.message)
        idlingResource.decrement()
      })
    }, { e ->
      e.printStackTrace()
      fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
  }
}