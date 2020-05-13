package com.twilio.verify.sample.view.challenges.update

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.sample.R
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.view.string
import kotlinx.android.synthetic.main.fragment_challenge.approveChallenge
import kotlinx.android.synthetic.main.fragment_challenge.challengeActionsGroup
import kotlinx.android.synthetic.main.fragment_challenge.challengeInfo
import kotlinx.android.synthetic.main.fragment_challenge.content
import kotlinx.android.synthetic.main.fragment_challenge.denyChallenge
import kotlinx.android.synthetic.main.view_factor.factorInfo
import org.koin.android.ext.android.inject
import java.text.DateFormat

const val ARG_CHALLENGE_SID = "challengeSid"
const val ARG_FACTOR_SID = "factorSid"

class ChallengeFragment : Fragment() {
  private lateinit var challengeSid: String
  private lateinit var factorSid: String
  private val twilioVerifyAdapter: TwilioVerifyAdapter by inject()

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
      twilioVerifyAdapter.getFactors(::showFactor) {
        it.showError(
            content
        )
      }
      getChallenge(challengeSid, factorSid)
    }
  }

  private fun showFactor(factors: List<Factor>) {
    factorInfo.text = factors.first { it.sid == factorSid }
        .string()
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
    twilioVerifyAdapter.updateChallenge(
        UpdatePushChallengeInput(challenge.factorSid, challenge.sid, status),
        { getChallenge(challenge.sid, challenge.factorSid) },
        { it.showError(content) }
    )
  }

  private fun getChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    twilioVerifyAdapter.getChallenge(
        challengeSid,
        factorSid,
        ::showChallenge
    ) { it.showError(content) }
  }
}
