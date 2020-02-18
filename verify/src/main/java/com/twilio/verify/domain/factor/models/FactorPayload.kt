/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

internal data class FactorPayload(
  val friendlyName: String,
  val type: FactorType,
  val binding: Map<String, Any>,
  val serviceSid: String,
  val entitySid: String
)