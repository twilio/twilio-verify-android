package com.twilio.verify.models

class ChallengeListInput(
  val factorSid: String,
  val status: ChallengeStatus?,
  val pageSize: Int,
  val pageToken: String?
)