/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import com.twilio.verify.api.APIResponses
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.PushFactorInput
import org.junit.After
import org.junit.Assert
import org.junit.Before

open class BaseFactorTest : BaseServerTest() {

  internal var factor: PushFactor? = null

  @Before
  override fun before() {
    createFactor()
    super.before()
  }

  @After
  override fun tearDown() {
    factor?.keyPairAlias?.let { alias ->
      keyStore.deleteEntry(alias)
    }
    super.tearDown()
  }

  private fun createFactor() {
    setupTwilioVerify()
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
      factor = it as PushFactor
      idlingResource.decrement()
    }, { e ->
      Assert.fail(e.message)
      idlingResource.decrement()
    })
    idlingResource.waitForResource()
    mockWebServer.takeRequest()
  }
}