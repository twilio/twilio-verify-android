/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput

internal class TwilioVerifyManager(private val factorFacade: FactorFacade) : TwilioVerify {
  override fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    TODO(
        "not implemented"
    )
  }
}