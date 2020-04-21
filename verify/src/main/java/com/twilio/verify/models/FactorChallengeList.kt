package com.twilio.verify.models

class FactorChallengeList(
  override val challenges: List<Challenge>,
  override val metadata: Metadata
) : ChallengeList