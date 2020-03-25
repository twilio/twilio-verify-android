/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

internal data class CreateFactorPayload(
  override val friendlyName: String,
  override val type: FactorType,
  override val pushToken: String,
  val publicKey: String,
  override val serviceSid: String,
  override val entity: String,
  val jwt: String
) : FactorPayload
