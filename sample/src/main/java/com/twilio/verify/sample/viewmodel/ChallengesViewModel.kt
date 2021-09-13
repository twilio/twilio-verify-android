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
import com.twilio.verify.models.ChallengeListOrder.Desc
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.sample.TwilioVerifyAdapter

private const val PAGE_SIZE = 20

class ChallengesViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val challenges: MutableLiveData<Challenges> = MutableLiveData()

  fun loadChallenges(factorSid: String) {
    twilioVerifyAdapter.getAllChallenges(
      ChallengeListPayload(factorSid, PAGE_SIZE, order = Desc),
      { challengeList ->
        challenges.value = ChallengeList(challengeList.challenges)
      },
      {
        challenges.value = ChallengesError(it)
      }
    )
  }

  fun getChallenges(): LiveData<Challenges> {
    return challenges
  }
}

sealed class Challenges
class ChallengeList(val challenges: List<Challenge>) : Challenges()
class ChallengesError(val exception: Exception) : Challenges()
