package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal interface FactorPayload {
  val friendlyName: String
  val type: FactorType
  val serviceSid: String
  val entity: String
  val pushToken: String
}