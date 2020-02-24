/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface Challenge {
  val sid: String
  val details: LinkedHashMap<String, String>
  val hiddenDetails: LinkedHashMap<String, String>
  val factorSid: String
  var status: ChallengeStatus
}

enum class ChallengeStatus {
  Pending,
  Approved,
  Denied,
  Expired
}