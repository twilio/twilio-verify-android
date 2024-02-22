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
import com.twilio.verify.AlreadyUpdatedChallengeException
import com.twilio.verify.ExpiredChallengeException
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.databinding.FragmentChallengeBinding
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.view.string
import com.twilio.verify.sample.viewmodel.ChallengeError
import com.twilio.verify.sample.viewmodel.ChallengeViewModel
import com.twilio.verify.sample.viewmodel.FactorError
import com.twilio.verify.sample.viewmodel.FactorViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.DateFormat
import org.koin.androidx.viewmodel.ext.android.viewModel

const val ARG_CHALLENGE_SID = "challengeSid"
const val ARG_FACTOR_SID = "factorSid"

class ChallengeFragment : Fragment() {
  private lateinit var challengeSid: String
  private lateinit var factorSid: String
  private val factorViewModel: FactorViewModel by activityViewModel()
  private val challengeViewModel: ChallengeViewModel by activityViewModel()
  private var _binding: FragmentChallengeBinding? = null
  private val binding get() = _binding!!

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
    _binding = FragmentChallengeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
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
              is FactorError -> it.exception.showError(binding.content)
            }
          }
        )
      factorViewModel.loadFactor(factorSid)
      challengeViewModel.getChallenge()
        .observe(
          viewLifecycleOwner,
          Observer {
            binding.approveButton.isEnabled = true
            binding.denyButton.isEnabled = true
            when (it) {
              is com.twilio.verify.sample.viewmodel.Challenge -> showChallenge(it.challenge)
              is ChallengeError -> handleError(it.exception)
            }
          }
        )
      challengeViewModel.loadChallenge(challengeSid, factorSid)
    }
  }

  private fun handleError(exception: Exception) {
    exception.showError(binding.content)
    if (exception.cause is ExpiredChallengeException || exception.cause is AlreadyUpdatedChallengeException) {
      challengeViewModel.loadChallenge(challengeSid, factorSid)
    }
  }

  private fun showFactor(factor: Factor) {
    binding.factor.factorSidText.apply {
      text = factor.sid
      setTextIsSelectable(true)
    }
    binding.factor.factorNameText.apply {
      text = factor.friendlyName
      setTextIsSelectable(true)
    }
    binding.factor.identityText.apply {
      text = factor.identity
      setTextIsSelectable(true)
    }
    binding.factor.factorStatusText.text = factor.status.value
  }

  private fun showChallenge(challenge: Challenge) {
    binding.challenge.challengeSidText.apply {
      text = challenge.sid
      setTextIsSelectable(true)
    }
    binding.challenge.challengeMessageText.apply {
      text = challenge.challengeDetails.message
      setTextIsSelectable(true)
    }
    binding.challenge.challengeStatusText.text = challenge.status.value
    binding.challenge.challengeCreatedAtText.text = DateUtils.formatDateTime(
      context,
      challenge.createdAt.time,
      DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
    )
    binding.challenge.challengeExpireOnText.text = DateUtils.formatDateTime(
      context,
      challenge.expirationDate.time,
      DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
    )
    binding.challengeActionsGroup?.visibility =
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
    binding.challengeInfoText?.text = info
    binding.approveButton?.setOnClickListener {
      updateChallenge(challenge, ChallengeStatus.Approved)
    }
    binding.denyButton?.setOnClickListener {
      updateChallenge(challenge, ChallengeStatus.Denied)
    }
  }

  private fun updateChallenge(
    challenge: Challenge,
    status: ChallengeStatus
  ) {
    binding.approveButton.isEnabled = false
    binding.denyButton.isEnabled = false
    challengeViewModel.updateChallenge(challenge, status)
  }
}
