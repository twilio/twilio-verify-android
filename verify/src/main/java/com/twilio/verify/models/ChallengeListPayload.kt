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

package com.twilio.verify.models

import com.twilio.verify.models.ChallengeListOrder.Asc

/**
 * Describes the information required to fetch a **ChallengeList**
 */
class ChallengeListPayload(
  /**
   * The unique SID identifier of the Factor to which the **ChallengeList** is related
   */
  val factorSid: String,
  /**
   * Number of Challenges to be returned by the service
   */
  val pageSize: Int,
  /**
   * Status to filter the Challenges, if nothing is sent, Challenges of all status will be returned
   */
  val status: ChallengeStatus? = null,
  /**
   * Sort challenges in order by creation date of the challenge
   */
  val order: ChallengeListOrder = Asc,
  /**
   * Token used to retrieve the next page in the pagination arrangement
   */
  val pageToken: String? = null
)

enum class ChallengeListOrder {
  Asc,
  Desc
}
