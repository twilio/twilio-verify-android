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
