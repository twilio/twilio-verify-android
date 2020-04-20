package com.twilio.verify.models

class ChallengeList(
  override val challenges: ArrayList<Challenge>,
  override val metadata: Metadata
) : ChallengeListInterface