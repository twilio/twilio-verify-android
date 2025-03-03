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

package com.twilio.verify.sample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.UpdatePushChallengePayload
import com.twilio.verify.sample.R
import com.twilio.verify.sample.R.id
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.databinding.ActivityMainBinding
import com.twilio.verify.sample.model.AppModel
import com.twilio.verify.sample.push.NewChallenge
import com.twilio.verify.sample.push.VerifyEventBus
import com.twilio.verify.sample.view.challenges.update.ARG_CHALLENGE_SID
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

  private lateinit var subscriberJob: Job

  val twilioVerifyAdapter: TwilioVerifyAdapter by inject()

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    showChallengeIfNeeded()
  }

  override fun onResume() {
    super.onResume()
    subscribeToEvents()
  }

  override fun onPause() {
    super.onPause()
    subscriberJob.cancel()
  }

  private fun showChallengeIfNeeded() {
    if (intent.hasExtra(ARG_FACTOR_SID) && intent.hasExtra(ARG_CHALLENGE_SID)) {
      showChallenge(intent.getStringExtra(ARG_FACTOR_SID)!!, intent.getStringExtra(ARG_CHALLENGE_SID)!!)
    }
  }

  private fun subscribeToEvents() {
    subscriberJob = lifecycleScope.launch {
      VerifyEventBus.consumeEvent<NewChallenge> {
        if (AppModel.silentlyApproveChallengesPerFactor[it.factorSid] == true) {
          approveChallenge(it.factorSid, it.challengeSid)
        } else {
          showChallenge(it.factorSid, it.challengeSid)
        }
      }
    }
  }

  private fun showChallenge(
    factorSid: String,
    challengeSid: String
  ) {
    val bundle = bundleOf(
      ARG_CHALLENGE_SID to challengeSid, ARG_FACTOR_SID to factorSid
    )
    findNavController(R.id.nav_host_fragment).navigate(id.action_show_challenge, bundle)
  }

  private fun approveChallenge(
    factorSid: String,
    challengeSid: String
  ) {
    twilioVerifyAdapter.updateChallenge(
      UpdatePushChallengePayload(
        factorSid,
        challengeSid,
        Approved
      ),
      {
        with(NotificationManagerCompat.from(this)) {
          cancel(challengeSid.hashCode())
        }
        showChallenge(factorSid, challengeSid)
      },
      {
        it.printStackTrace()
      }
    )
  }
}
