/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.domain.factor.models.FactorPayload
import com.twilio.verify.models.Factor

internal interface FactorProvider {
  fun create(factorPayload: FactorPayload, success: (Factor?) -> Unit)
  fun get(sid: String): Factor?
  fun update(factor: Factor): Factor?
}