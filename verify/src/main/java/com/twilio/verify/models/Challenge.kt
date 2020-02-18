/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface Challenge {
  val sid: String
  val details: LinkedHashMap<String, String>
  val hiddenDetails: String
  val factorSid: String
  var status: ChallengeStatus
}

enum class ChallengeStatus(val value: String) {
  Pending("pending"),
  Expired("expired"),
  Approved("approved"),
  Denied("denied")
}