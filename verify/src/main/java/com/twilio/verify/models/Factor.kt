/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface Factor {
  var status: FactorStatus
  val sid: String
  val friendlyName: String
  val accountSid: String
  val serviceSid: String
  val entityIdentity: String
  val type: FactorType
  val credentialSid: String
}

enum class FactorStatus(val value: String) {
  Verified("verified"),
  Unverified("unverified")
}