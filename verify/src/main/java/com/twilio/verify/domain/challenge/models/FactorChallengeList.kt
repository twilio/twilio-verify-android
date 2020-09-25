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
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.Metadata

/**
 * Describes the information of a **ChallengeList**
 */
internal class FactorChallengeList(
  /**
   * List of Challenges that matches the parameters of the **ChallengeListPayload** used
   */
  override val challenges: List<Challenge>,
  /**
   * Metadata returned by the /Challenges endpoint, used to fetch subsequent pages of Challenges
   */
  override val metadata: Metadata
) : ChallengeList
