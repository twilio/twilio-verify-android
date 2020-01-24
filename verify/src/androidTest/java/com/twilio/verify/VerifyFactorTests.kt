package com.twilio.verify

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.verify.TwilioVerify.Builder
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.api.APIResponses
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import com.twilio.verify.networking.NetworkException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class VerifyFactorTests : BaseServerTest() {

  private lateinit var twilioVerify: TwilioVerify

  override fun before() {
    super.before()
    val context: Context = InstrumentationRegistry.getInstrumentation()
        .targetContext
    val authorization = Authorization("accountSid", "authToken")
    twilioVerify = Builder(context, authorization)
        .networkProvider(NetworkAdapter(httpsURLConnection))
        .build()
  }

  @Test
  fun testVerifyFactorWithValidAPIResponseShouldReturnFactor() {
    val sid = "sid"
    val verifyInput = VerifyPushFactorInput(sid)
    enqueueMockResponse(200, APIResponses.verifyValidFactorResponse())
    twilioVerify.verifyFactor(verifyInput, {
      assertEquals(sid, it.sid)
    }, {
      fail()
    })
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
    twilioVerify.verifyFactor(verifyInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
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
    twilioVerify.verifyFactor(verifyInput, {
      fail()
    }, { exception ->
      assertEquals(expectedException.message, exception.message)
    })
  }
}