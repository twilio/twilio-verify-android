package com.twilio.verify.api

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.verify.BaseServerTest
import com.twilio.verify.api.APIResponses.createFactorResponse
import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization
import com.twilio.verify.networking.NetworkAdapter
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class FactorAPIClientTests : BaseServerTest() {
  private lateinit var factorAPIClient: FactorAPIClient

  override fun before() {
    super.before()
    val context: Context = InstrumentationRegistry.getInstrumentation()
        .targetContext
    val authorization = Authorization("accountSid", "authToken")
    factorAPIClient = FactorAPIClient(
        NetworkAdapter(httpsURLConnection as HttpURLConnection), context, authorization
    )
  }

  @Test
  fun qweasd() {
    val factorPayload = FactorPayload("", Push, mapOf(), "serviceSid", "entityId")
    enqueueMockResponse(200, createFactorResponse())
    factorAPIClient.create(factorPayload, {
      print(it.toString())
    }, {
      fail()
    })
  }
}