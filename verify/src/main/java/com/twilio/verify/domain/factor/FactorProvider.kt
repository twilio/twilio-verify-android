/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.domain.factor.models.CreateFactorPayload
import com.twilio.verify.domain.factor.models.UpdateFactorPayload
import com.twilio.verify.models.Factor

internal interface FactorProvider {
  fun create(
    createFactorPayload: CreateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun verify(
    factor: Factor,
    payload: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun update(
    factor: Factor,
    updateFactorPayload: UpdateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun delete(
    factor: Factor,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  )

  fun get(sid: String): Factor?
  fun getAll(): List<Factor>
  fun save(factor: Factor): Factor?
}