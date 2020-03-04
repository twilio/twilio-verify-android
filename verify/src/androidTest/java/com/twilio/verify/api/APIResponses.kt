package com.twilio.verify.api

import androidx.test.platform.app.InstrumentationRegistry

/*
 * Copyright (c) 2020, Twilio Inc.
 */

object APIResponses {
  fun createValidFactorResponse() = getJson("network_files/factor/create_factor_valid.json")

  fun createInvalidFactorResponse() = getJson("network_files/factor/create_factor_invalid.json")

  fun verifyValidFactorResponse() = getJson("network_files/factor/verify_factor_valid.json")

  fun verifyInvalidFactorResponse() = getJson("network_files/factor/verify_factor_invalid.json")

  fun getValidPendingChallengeResponse() = getJson("network_files/challenge/get_challenge_pending_valid.json")

  fun getValidApprovedChallengeResponse() = getJson("network_files/challenge/get_challenge_approved_valid.json")

  private fun getJson(path: String): String =
    String(
        InstrumentationRegistry.getInstrumentation().targetContext.assets.open(path).readBytes()
    )
}
