/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.sample.TwilioVerifyAdapter

class ChallengeViewModel(private val twilioVerifyAdapter: TwilioVerifyAdapter) : ViewModel() {
  private val challenge: MutableLiveData<ChallengeResult> = MutableLiveData()

  fun loadChallenge(
    sid: String,
    factorSid: String
  ) {
    twilioVerifyAdapter.getChallenge(sid, factorSid, {
      challenge.value = Challenge(it)
    }, {
      challenge.value = ChallengeError(it)
    })
  }

  fun getChallenge(): LiveData<ChallengeResult> {
    return challenge
  }

  fun updateChallenge(
    challenge: Challenge,
    status: ChallengeStatus
  ) {
    twilioVerifyAdapter.updateChallenge(
        UpdatePushChallengeInput(challenge.factorSid, challenge.sid, status), {
      loadChallenge(challenge.sid, challenge.factorSid)
    }, {
      this.challenge.value = ChallengeError(it)
    })
  }
}

sealed class ChallengeResult
class Challenge(val challenge: Challenge) : ChallengeResult()
class ChallengeError(val exception: Exception) : ChallengeResult()