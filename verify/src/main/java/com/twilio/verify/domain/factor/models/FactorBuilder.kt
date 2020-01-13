/*
 * Copyright (c) 2019, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

internal class FactorBuilder {

  lateinit var friendlyName: String
    private set
  lateinit var type: FactorType
    private set
  lateinit var binding: Map<String, Any>
    private set
  lateinit var serviceSid: String
    private set
  lateinit var userId: String
    private set

  fun friendlyName(friendlyName: String) = apply { this.friendlyName = friendlyName }
  fun type(type: FactorType) = apply { this.type = type }
  fun binding(binding: Map<String, Any>) = apply { this.binding = binding }
  fun serviceSid(serviceSid: String) = apply { this.serviceSid = serviceSid }
  fun userId(userId: String) = apply { this.userId = userId }
}