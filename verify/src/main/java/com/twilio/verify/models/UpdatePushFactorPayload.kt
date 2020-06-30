package com.twilio.verify.models

/*
 * Copyright (c) 2020, Twilio Inc.
 */

data class UpdatePushFactorPayload(
  override val sid: String,
  val pushToken: String
) : UpdateFactorPayload