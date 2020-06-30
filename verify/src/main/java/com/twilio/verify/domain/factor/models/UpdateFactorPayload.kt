package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal data class UpdateFactorPayload(
  override val friendlyName: String,
  override val type: FactorType,
  override val serviceSid: String,
  override val entity: String,
  override val config: Map<String, String>,
  val factorSid: String
) : FactorDataPayload