package com.twilio.verify.models

interface ChallengeListInput {
  val factorSid: String
  val status: ChallengeStatus?
  val pageSize: Int
  val pageToken: String?
}