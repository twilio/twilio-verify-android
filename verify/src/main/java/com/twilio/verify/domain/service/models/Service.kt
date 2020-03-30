package com.twilio.verify.domain.service.models

import java.util.Date

/*
 * Copyright (c) 2020, Twilio Inc.
 */

data class Service(
  val sid: String,
  val createdDate: Date,
  val updatedDate: Date,
  val friendlyName: String,
  val accountSid: String
)