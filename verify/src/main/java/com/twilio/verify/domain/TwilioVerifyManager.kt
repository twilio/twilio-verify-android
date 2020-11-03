/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.domain

import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.domain.challenge.ChallengeFacade
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.domain.service.ServiceFacade
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorPayload
import com.twilio.verify.models.UpdateChallengePayload
import com.twilio.verify.models.UpdateFactorPayload
import com.twilio.verify.models.VerifyFactorPayload

internal class TwilioVerifyManager(
  private val factorFacade: FactorFacade,
  private val challengeFacade: ChallengeFacade,
  private val serviceFacade: ServiceFacade
) : TwilioVerify {
  override fun createFactor(
    factorPayload: FactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.createFactor(factorPayload, success, error)
  }

  override fun verifyFactor(
    verifyFactorPayload: VerifyFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.verifyFactor(verifyFactorPayload, success, error)
  }

  override fun updateFactor(
    updateFactorPayload: UpdateFactorPayload,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.updateFactor(updateFactorPayload, success, error)
  }

  override fun getAllFactors(
    success: (List<Factor>) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.getAllFactors(success, error)
  }

  override fun getChallenge(
    challengeSid: String,
    factorSid: String,
    success: (Challenge) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    challengeFacade.getChallenge(challengeSid, factorSid, success, error)
  }

  override fun getAllChallenges(
    challengeListPayload: ChallengeListPayload,
    success: (ChallengeList) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    challengeFacade.getAllChallenges(challengeListPayload, success, error)
  }

  override fun updateChallenge(
    updateChallengePayload: UpdateChallengePayload,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    challengeFacade.updateChallenge(updateChallengePayload, success, error)
  }

  override fun deleteFactor(
    factorSid: String,
    success: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    factorFacade.deleteFactor(factorSid, success, error)
  }

  override fun clearLocalStorage(then: () -> Unit) {
    factorFacade.clearLocalStorage(then)
  }
}
