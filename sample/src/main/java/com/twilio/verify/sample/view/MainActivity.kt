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
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.twilio.verify.sample.R.id
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.push.NewChallenge
import com.twilio.verify.sample.push.VerifyEventBus
import com.twilio.verify.sample.view.challenges.update.ARG_CHALLENGE_SID
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import kotlinx.android.synthetic.main.activity_main.nav_host_fragment

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_main)
    subscribeToEvents()
    showChallengeIfNeeded()
  }

  private fun showChallengeIfNeeded() {
    if (intent.hasExtra(ARG_FACTOR_SID) && intent.hasExtra(ARG_CHALLENGE_SID)) {
      showChallenge(intent.getStringExtra(ARG_FACTOR_SID), intent.getStringExtra(ARG_CHALLENGE_SID))
    }
  }

  private fun subscribeToEvents() {
    VerifyEventBus.consumeEvent<NewChallenge> {
      showChallenge(it.factorSid, it.challengeSid)
    }
  }

  private fun showChallenge(
    factorSid: String,
    challengeSid: String
  ) {
    val bundle = bundleOf(
      ARG_CHALLENGE_SID to challengeSid, ARG_FACTOR_SID to factorSid
    )
    nav_host_fragment?.findNavController()
      ?.navigate(id.action_show_challenge, bundle)
  }
}
