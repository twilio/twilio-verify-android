package com.twilio.verify.models

class ChallengeListInput(
  val factorSid: String,
  val pageSize: Int,
  val status: ChallengeStatus? = null,
  val pageToken: String? = null
)