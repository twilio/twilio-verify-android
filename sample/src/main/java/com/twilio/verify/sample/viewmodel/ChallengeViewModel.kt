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

package com.twilio.verify.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.UpdatePushChallengePayload
import com.twilio.verify.sample.TwilioVerifyAdapter

class ChallengeViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val challenge: MutableLiveData<ChallengeResult> = MutableLiveData()

  fun loadChallenge(
    sid: String,
    factorSid: String
  ) {
    twilioVerifyAdapter.getChallenge(
      sid, factorSid,
      {
        challenge.value = Challenge(it)
      },
      {
        challenge.value = ChallengeError(it)
      }
    )
  }

  fun getChallenge(): LiveData<ChallengeResult> {
    return challenge
  }

  fun updateChallenge(
    challenge: Challenge,
    status: ChallengeStatus
  ) {
    twilioVerifyAdapter.updateChallenge(
      UpdatePushChallengePayload(challenge.factorSid, challenge.sid, status),
      {
        loadChallenge(challenge.sid, challenge.factorSid)
      },
      {
        this.challenge.value = ChallengeError(it)
      }
    )
  }
}

sealed class ChallengeResult
class Challenge(val challenge: Challenge) : ChallengeResult()
class ChallengeError(val exception: Exception) : ChallengeResult()
