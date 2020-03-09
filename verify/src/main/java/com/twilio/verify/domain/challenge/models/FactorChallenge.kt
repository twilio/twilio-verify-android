/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge.models

import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor
import java.util.Date

internal class FactorChallenge(
  override val sid: String,
  override val challengeDetails: ChallengeDetails,
  override val hiddenDetails: String,
  override val factorSid: String,
  override var status: ChallengeStatus,
  override val createdAt: Date,
  override val updatedAt: Date,
  override val expirationDate: Date,
    // Original values to generate signature
  internal val details: String,
  internal val createdDate: String,
  internal val updatedDate: String,
  internal val entitySid: String
) : Challenge {
  internal var factor: Factor? = null
}