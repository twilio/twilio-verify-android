/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

class PushFactorPayload(
  override val friendlyName: String,
  override val serviceSid: String,
  override val identity: String,
  val pushToken: String,
  val accessToken: String
) : FactorPayload {
  override val factorType: FactorType
    get() = FactorType.PUSH
}