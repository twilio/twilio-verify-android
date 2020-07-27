/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

import java.util.Date

interface Factor {
  var status: FactorStatus
  val sid: String
  val friendlyName: String
  val accountSid: String
  val serviceSid: String
  val identity: String
  val type: FactorType
  val createdAt: Date
}

enum class FactorStatus(val value: String) {
  Verified("verified"),
  Unverified("unverified")
}