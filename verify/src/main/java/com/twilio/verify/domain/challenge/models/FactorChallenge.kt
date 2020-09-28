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

package com.twilio.verify.domain.challenge.models

import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor
import java.util.Date
import org.json.JSONObject

internal class FactorChallenge(
  override val sid: String,
  override val challengeDetails: ChallengeDetails,
  override val hiddenDetails: String,
  override val factorSid: String,
  override var status: ChallengeStatus,
  override val createdAt: Date,
  override val updatedAt: Date,
  override val expirationDate: Date,
  // Original values to generate signature
  internal val signatureFields: List<String>? = null,
  internal val response: JSONObject? = null
) : Challenge {
  internal var factor: Factor? = null
}
