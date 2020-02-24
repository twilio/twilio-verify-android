package com.twilio.verify.models

/*
 * Copyright (c) 2020, Twilio Inc.
 */

class VerifyPushFactorInput(
  override val sid: String,
  val verificationCode: String
) : VerifyFactorInput