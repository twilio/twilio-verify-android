package com.twilio.verify.models

/*
 * Copyright (c) 2020, Twilio Inc.
 */

data class UpdatePushFactorInput(
  override val sid: String,
  val pushToken: String
) : UpdateFactorInput