package com.twilio.verify.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider

/*
 * Copyright (c) 2020, Twilio Inc.
 */

object APIResponses {
  fun createValidFactorResponse() = getJson("network_files/factor/create_factor_valid.json")

  fun createInvalidFactorResponse() = getJson("network_files/factor/create_factor_invalid.json")

  fun updateFactorValidResponse() = getJson("network_files/factor/update_factor_valid.json")

  fun updateFactorInvalidResponse() = getJson("network_files/factor/update_factor_invalid.json")

  fun verifyValidFactorResponse() = getJson("network_files/factor/verify_factor_valid.json")

  fun verifyInvalidFactorResponse() = getJson("network_files/factor/verify_factor_invalid.json")

  fun getValidPendingChallengeResponse() = getJson("network_files/challenge/get_challenge_pending_valid.json")

  fun getValidApprovedChallengeResponse() = getJson("network_files/challenge/get_challenge_approved_valid.json")

  fun getAllChallengesResponse() = getJson("network_files/challenges/get_challenges_valid.json")

  private fun getJson(path: String): String =
    String(
      ApplicationProvider.getApplicationContext<Context>().assets.open(path).readBytes()
    )
}
