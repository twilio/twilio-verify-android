/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

import java.util.Date

interface Challenge {
  val sid: String
  val challengeDetails: ChallengeDetails
  val hiddenDetails: String
  val factorSid: String
  val status: ChallengeStatus
  val createdAt: Date
  val updatedAt: Date
  val expirationDate: Date
}

enum class ChallengeStatus(val value: String) {
  Pending("pending"),
  Approved("approved"),
  Denied("denied"),
  Expired("expired")
}

data class ChallengeDetails(
  val message: String,
  val fields: List<Detail>,
  val date: Date?
)

data class Detail(
  val label: String,
  val value: String
)