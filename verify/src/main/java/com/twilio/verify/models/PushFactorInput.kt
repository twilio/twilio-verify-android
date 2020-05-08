/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

class PushFactorInput(
  override val friendlyName: String,
  override val serviceSid: String,
  override val identity: String,
  override val factorType: FactorType,
  val pushToken: String,
  val jwt: String
) : FactorInput