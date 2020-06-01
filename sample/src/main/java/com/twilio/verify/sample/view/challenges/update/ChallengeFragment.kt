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
import kotlinx.android.synthetic.main.fragment_challenge.approveChallenge
import kotlinx.android.synthetic.main.fragment_challenge.challengeActionsGroup
import kotlinx.android.synthetic.main.fragment_challenge.challengeInfo
import kotlinx.android.synthetic.main.fragment_challenge.content
import kotlinx.android.synthetic.main.fragment_challenge.denyChallenge
import kotlinx.android.synthetic.main.view_factor.factorInfo
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat

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
          .observe(viewLifecycleOwner, Observer {
            when (it) {
              is com.twilio.verify.sample.viewmodel.Factor -> showFactor(it.factor)
              is FactorError -> it.exception.showError(content)
            }
          })
      challengeViewModel.getChallenge()
          .observe(viewLifecycleOwner, Observer {
            approveChallenge.isEnabled = true
            denyChallenge.isEnabled = true
            when (it) {
              is com.twilio.verify.sample.viewmodel.Challenge -> showChallenge(it.challenge)
              is ChallengeError -> it.exception.showError(content)
            }
          })
      factorViewModel.loadFactor(factorSid)
      challengeViewModel.loadChallenge(challengeSid, factorSid)
    }
  }

  private fun showFactor(factor: Factor) {
    factorInfo.text = factor.string()
  }

  private fun showChallenge(challenge: Challenge) {
    challengeActionsGroup?.visibility =
      if (challenge.status == ChallengeStatus.Pending) View.VISIBLE else View.GONE
    val info = "${challenge.challengeDetails.message}\nStatus: ${challenge.status.value}\n" +
        "${challenge.challengeDetails.string(context)}Sid:\n ${challenge.sid}\n" +
        "Expire on: ${DateUtils.formatSameDayTime(
            challenge.expirationDate.time, System.currentTimeMillis(), DateFormat.MEDIUM,
            DateFormat.MEDIUM
        )}\n" +
        "Updated at: ${DateUtils.formatSameDayTime(
            challenge.updatedAt.time, System.currentTimeMillis(), DateFormat.MEDIUM,
            DateFormat.MEDIUM
        )}"
    challengeInfo?.text = info
    approveChallenge?.setOnClickListener {
      updateChallenge(challenge, ChallengeStatus.Approved)
    }
    denyChallenge?.setOnClickListener {
      updateChallenge(challenge, ChallengeStatus.Denied)
    }
  }

  private fun updateChallenge(
    challenge: Challenge,
    status: ChallengeStatus
  ) {
    approveChallenge.isEnabled = false
    denyChallenge.isEnabled = false
    challengeViewModel.updateChallenge(challenge, status)
  }
}
