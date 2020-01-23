/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.VerifyFactorInput

internal class TwilioVerifyManager(private val factorFacade: FactorFacade) : TwilioVerify {
  override fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.createFactor(factorInput, success, error)
  }

  override fun verifyFactor(
    verifyFactorInput: VerifyFactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.verifyFactor(verifyFactorInput, success, error)
  }
}