package com.twilio.verify.models

class FactorChallengeListInput(
  override val factorSid: String,
  override val status: ChallengeStatus?,
  override val pageSize: Int,
  override val pageToken: String?
) : ChallengeListInput