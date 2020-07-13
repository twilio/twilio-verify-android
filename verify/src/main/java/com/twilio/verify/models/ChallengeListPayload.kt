package com.twilio.verify.models

class ChallengeListPayload(
  val factorSid: String,
  val pageSize: Int,
  val status: ChallengeStatus? = null,
  val pageToken: String? = null
)