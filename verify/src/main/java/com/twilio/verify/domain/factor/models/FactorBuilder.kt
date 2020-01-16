/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.FactorType

internal class FactorBuilder {

  var friendlyName: String? = null
    private set
  var type: FactorType? = null
    private set
  var binding: Map<String, Any> = emptyMap()
    private set
  var serviceSid: String? = null
    private set
  var entityId: String? = null
    private set

  fun friendlyName(friendlyName: String) = apply { this.friendlyName = friendlyName }
  fun type(type: FactorType) = apply { this.type = type }
  fun binding(binding: Map<String, Any>) = apply { this.binding = binding }
  fun serviceSid(serviceSid: String) = apply { this.serviceSid = serviceSid }
  fun entityId(entityId: String) = apply { this.entityId = entityId }
}