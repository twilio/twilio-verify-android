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

package com.twilio.verify.sample.view.challenges.update

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.R
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.view.string
import com.twilio.verify.sample.viewmodel.ChallengeError
import com.twilio.verify.sample.viewmodel.ChallengeViewModel
import com.twilio.verify.sample.viewmodel.FactorError
import com.twilio.verify.sample.viewmodel.FactorViewModel
import java.text.DateFormat
import kotlinx.android.synthetic.main.fragment_challenge.approveButton
import kotlinx.android.synthetic.main.fragment_challenge.challengeActionsGroup
import kotlinx.android.synthetic.main.fragment_challenge.challengeInfoText
import kotlinx.android.synthetic.main.fragment_challenge.content
import kotlinx.android.synthetic.main.fragment_challenge.denyButton
import kotlinx.android.synthetic.main.view_challenge.challengeCreatedAtText
import kotlinx.android.synthetic.main.view_challenge.challengeExpireOnText
import kotlinx.android.synthetic.main.view_challenge.challengeNameText
import kotlinx.android.synthetic.main.view_challenge.challengeSidText
import kotlinx.android.synthetic.main.view_challenge.challengeStatusText
import kotlinx.android.synthetic.main.view_factor.factorNameText
import kotlinx.android.synthetic.main.view_factor.factorSidText
import kotlinx.android.synthetic.main.view_factor.factorStatusText
import kotlinx.android.synthetic.main.view_factor.identityText
import org.koin.androidx.viewmodel.ext.android.viewModel

const val ARG_CHALLENGE_SID = "challengeSid"
const val ARG_FACTOR_SID = "factorSid"

class ChallengeFragment : Fragment() {
  private lateinit var challengeSid: String
  private lateinit var factorSid: String
  private val factorViewModel: FactorViewModel by viewModel()
  private val challengeViewModel: ChallengeViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      challengeSid = it.getString(
        ARG_CHALLENGE_SID
      ) ?: ""
      factorSid = it.getString(
        ARG_FACTOR_SID
      ) ?: ""
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_challenge, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    if (challengeSid.isNotEmpty() && factorSid.isNotEmpty()) {
      factorViewModel.getFactor()
        .observe(
          viewLifecycleOwner,
          Observer {
            when (it) {
              is com.twilio.verify.sample.viewmodel.Factor -> showFactor(it.factor)
              is FactorError -> it.exception.showError(content)
            }
          }
        )
      challengeViewModel.getChallenge()
        .observe(
          viewLifecycleOwner,
          Observer {
            approveButton.isEnabled = true
            denyButton.isEnabled = true
            when (it) {
              is com.twilio.verify.sample.viewmodel.Challenge -> showChallenge(it.challenge)
              is ChallengeError -> it.exception.showError(content)
            }
          }
        )
      factorViewModel.loadFactor(factorSid)
      challengeViewModel.loadChallenge(challengeSid, factorSid)
    }
  }

  private fun showFactor(factor: Factor) {
    factorSidText.apply {
      text = factor.sid
      setTextIsSelectable(true)
    }
    factorNameText.apply {
      text = factor.friendlyName
      setTextIsSelectable(true)
    }
    identityText.apply {
      text = factor.identity
      setTextIsSelectable(true)
    }
    factorStatusText.text = factor.status.value
  }

  private fun showChallenge(challenge: Challenge) {
    challengeSidText.apply {
      text = challenge.sid
      setTextIsSelectable(true)
    }
    challengeNameText.apply {
      text = challenge.challengeDetails.message
      setTextIsSelectable(true)
    }
    challengeStatusText.text = challenge.status.value
    challengeCreatedAtText.text = DateUtils.formatDateTime(
      context,
      challenge.createdAt.time,
      DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
    )
    challengeExpireOnText.text = DateUtils.formatDateTime(
      context,
      challenge.expirationDate.time,
      DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
    )
    challengeActionsGroup?.visibility =
      if (challenge.status == ChallengeStatus.Pending) View.VISIBLE else View.GONE
    val info = "Updated at: " +
      "${
      DateUtils.formatSameDayTime(
        challenge.updatedAt.time, System.currentTimeMillis(), DateFormat.MEDIUM,
        DateFormat.MEDIUM
      )
      }\n" + challenge.challengeDetails.string(context) +
      (
        challenge.hiddenDetails?.let { hiddenDetails ->
          "Hidden details:\n ${
          hiddenDetails.map {
            "  ${it.key} = ${it.value}"
          }.joinToString("\n")
          }"
        } ?: ""
        )
    challengeInfoText?.text = info
    approveButton?.setOnClickListener {
      updateChallenge(challenge, ChallengeStatus.Approved)
    }
    denyButton?.setOnClickListener {
      updateChallenge(challenge, ChallengeStatus.Denied)
    }
  }

  private fun updateChallenge(
    challenge: Challenge,
    status: ChallengeStatus
  ) {
    approveButton.isEnabled = false
    denyButton.isEnabled = false
    challengeViewModel.updateChallenge(challenge, status)
  }
}
