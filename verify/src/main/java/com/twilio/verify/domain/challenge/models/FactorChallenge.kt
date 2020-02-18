/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge.models

import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor

internal class FactorChallenge(
  override val sid: String,
  override val details: LinkedHashMap<String, String>,
  override val hiddenDetails: LinkedHashMap<String, String>,
  override val factorSid: String,
  override var status: ChallengeStatus
) : Challenge {
  internal var factor: Factor? = null
}