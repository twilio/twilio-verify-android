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
