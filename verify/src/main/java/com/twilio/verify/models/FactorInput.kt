/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface FactorInput {
  val friendlyName: String
  val serviceSid: String
  val identity: String
  val factorType: FactorType
}