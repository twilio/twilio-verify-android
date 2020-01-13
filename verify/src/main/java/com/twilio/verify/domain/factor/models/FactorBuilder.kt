/*
 * Copyright (c) 2019, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

internal class FactorBuilder {

  lateinit var friendlyName: String
  lateinit var type: FactorType
  lateinit var binding: Map<String, Any>
  lateinit var serviceSid: String
  lateinit var userId: String
}