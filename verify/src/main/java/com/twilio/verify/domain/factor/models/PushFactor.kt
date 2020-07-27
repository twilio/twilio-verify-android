/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType
import com.twilio.verify.models.FactorType.PUSH
import java.util.Date

internal class PushFactor(
  override val sid: String,
  override val friendlyName: String,
  override val accountSid: String,
  override val serviceSid: String,
  override val identity: String,
  override var status: FactorStatus = Unverified,
  override val createdAt: Date,
  val config: Config
) : Factor {
  override val type: FactorType = PUSH

  var keyPairAlias: String? = null
}

internal data class Config(internal val credentialSid: String)