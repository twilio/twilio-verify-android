/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

class PushFactorInput(
  override val friendlyName: String,
  val pushToken: String,
  val jwt: String
) : FactorInput