/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.sample.TwilioVerifyAdapter

private const val PAGE_SIZE = 20

class ChallengesViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val challenges: MutableLiveData<Challenges> = MutableLiveData()

  fun loadChallenges(factorSid: String) {
    twilioVerifyAdapter.getAllChallenges(ChallengeListPayload(factorSid, PAGE_SIZE), {
      challenges.value = ChallengeList(it.challenges)
    }, {
      challenges.value = ChallengesError(it)
    })
  }

  fun getChallenges(): LiveData<Challenges> {
    return challenges
  }
}

sealed class Challenges
class ChallengeList(val challenges: List<Challenge>) : Challenges()
class ChallengesError(val exception: Exception) : Challenges()
