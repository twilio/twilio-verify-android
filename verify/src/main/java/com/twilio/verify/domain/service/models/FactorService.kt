package com.twilio.verify.domain.service.models

import com.twilio.verify.models.Service
import java.util.Date

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal data class FactorService(
  override val sid: String,
  override val createdDate: Date,
  override val updatedDate: Date,
  override val friendlyName: String,
  override val accountSid: String
) : Service
