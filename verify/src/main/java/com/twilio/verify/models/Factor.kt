/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface Factor {
  val status: FactorStatus
  val sid: String
  val friendlyName: String
  val accountSid: String
  val serviceSid: String
  val entitySid: String
  val entityId: String
  val type: FactorType
}

enum class FactorStatus(val value: String) {
  Verified("verified"),
  Unverified("unverified")
}