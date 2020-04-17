package com.twilio.verify.models

class ChallengeList(
  override val challenges: ArrayList<Challenge>,
  override val metaData: MetaData
) : ChallengeListInterface