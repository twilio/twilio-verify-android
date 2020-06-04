/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

internal data class CreateFactorPayload(
  override val friendlyName: String,
  override val type: FactorType,
  override val serviceSid: String,
  override val entity: String,
  override val config: Map<String, String>,
  val binding: Map<String, String>,
  val jwe: String
) : FactorPayload
