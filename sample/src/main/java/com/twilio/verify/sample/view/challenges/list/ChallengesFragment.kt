package com.twilio.verify.sample.view.challenges.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListInput
import com.twilio.verify.sample.R
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.view.challenges.update.ARG_CHALLENGE_SID
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.view.string
import kotlinx.android.synthetic.main.fragment_factor_challenges.challenges
import kotlinx.android.synthetic.main.fragment_factor_challenges.content
import kotlinx.android.synthetic.main.fragment_factor_challenges.serviceInfo
import kotlinx.android.synthetic.main.view_factor.factorInfo
import org.koin.android.ext.android.inject

class ChallengesFragment : Fragment() {
  private lateinit var factorSid: String
  private lateinit var viewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewManager: RecyclerView.LayoutManager
  private val twilioVerifyAdapter: TwilioVerifyAdapter by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      factorSid = it.getString(ARG_FACTOR_SID) ?: ""
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_factor_challenges, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewManager = LinearLayoutManager(view?.context)
    showFactor()
    loadChallenges()
  }

  private fun showFactor() {
    twilioVerifyAdapter.getFactors({ factors ->
      val factor = factors.first { it.sid == factorSid }
      factorInfo.text = factor.string()
      factorInfo.setTextIsSelectable(true)
      showService(factor.serviceSid)
    }) {
      it.showError(content)
    }
  }

  private fun showService(serviceSid: String) {
    twilioVerifyAdapter.getService(serviceSid, {
      serviceInfo.text = it.string()
    }) {
      it.showError(content)
    }
  }

  private fun loadChallenges() {
    twilioVerifyAdapter.getAllChallenges(
        ChallengeListInput(factorSid, 20), ::showChallenges
    ) { exception ->
      exception.showError(content)
    }
  }

  private fun showChallenges(challengeList: ChallengeList) {
    viewAdapter = ChallengesAdapter(challengeList.challenges) {
      val bundle = bundleOf(
          ARG_FACTOR_SID to it.factorSid,
          ARG_CHALLENGE_SID to it.sid
      )
      findNavController().navigate(R.id.action_show_challenge, bundle)
    }
    challenges.apply {
      setHasFixedSize(true)
      layoutManager = viewManager
      adapter = viewAdapter
    }
  }
}
