/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.domain.challenge.ChallengeFacade
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.UpdateChallengeInput
import com.twilio.verify.models.VerifyFactorInput

internal class TwilioVerifyManager(
  private val factorFacade: FactorFacade,
  private val challengeFacade: ChallengeFacade
) : TwilioVerify {
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

  override fun updateChallenge(
    updateChallengeInput: UpdateChallengeInput,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    challengeFacade.updateChallenge(updateChallengeInput, success, error)
  }
}