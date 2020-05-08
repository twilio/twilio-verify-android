/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

class PushFactorInput(
  override val friendlyName: String,
  override val serviceSid: String,
  override val identity: String,
  val pushToken: String,
  val jwt: String
) : FactorInput {
  override val factorType: FactorType
    get() = FactorType.PUSH
}