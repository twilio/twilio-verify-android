/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorType
import com.twilio.verify.models.FactorType.Push

internal class PushFactor(
  override val sid: String,
  override val friendlyName: String,
  override val accountSid: String,
  override val serviceSid: String,
  override val entitySid: String,
  override val entityId: String
) : Factor {
  override val type: FactorType = Push

  var keyPairAlias: String? = null
}