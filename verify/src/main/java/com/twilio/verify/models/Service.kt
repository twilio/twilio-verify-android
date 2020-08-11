package com.twilio.verify.models

import java.util.Date

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal interface Service {
  val sid: String
  val createdDate: Date
  val updatedDate: Date
  val friendlyName: String
  val accountSid: String
}