package com.twilio.verify.api

import androidx.test.platform.app.InstrumentationRegistry

/*
 * Copyright (c) 2020, Twilio Inc.
 */

object APIResponses {
  fun createFactorResponse() = getJson("network_files/factor/create.json")

  private fun getJson(path: String): String =
    String(
        InstrumentationRegistry.getInstrumentation().targetContext.assets.open(path).readBytes()
    )
}
