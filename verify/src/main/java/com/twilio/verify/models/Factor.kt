/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface Factor {
  val sid: String
  val friendlyName: String
  val accountSid: String
  val serviceSid: String
  val entitySid: String
  val userId: String
  val type: FactorType
}