package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal data class UpdateFactorPayload(
  override val friendlyName: String,
  override val type: FactorType,
  override val pushToken: String,
  override val serviceSid: String,
  override val entity: String,
  val factorSid: String
) : FactorPayload