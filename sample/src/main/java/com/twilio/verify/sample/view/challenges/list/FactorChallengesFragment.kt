package com.twilio.verify.sample.view.challenges.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twilio.verify.models.Challenge
import com.twilio.verify.sample.R
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.view.challenges.update.ARG_CHALLENGE_SID
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.view.string
import com.twilio.verify.sample.viewmodel.ChallengeList
import com.twilio.verify.sample.viewmodel.ChallengesError
import com.twilio.verify.sample.viewmodel.ChallengesViewModel
import com.twilio.verify.sample.viewmodel.Factor
import com.twilio.verify.sample.viewmodel.FactorError
import com.twilio.verify.sample.viewmodel.FactorViewModel
import kotlinx.android.synthetic.main.fragment_factor_challenges.challenges
import kotlinx.android.synthetic.main.fragment_factors.content
import kotlinx.android.synthetic.main.view_factor.factorInfo
import org.koin.androidx.viewmodel.ext.android.viewModel

class FactorChallengesFragment : Fragment() {
  private lateinit var factorSid: String
  private lateinit var viewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewManager: RecyclerView.LayoutManager
  private val factorViewModel: FactorViewModel by viewModel()
  private val challengesViewModel: ChallengesViewModel by viewModel()

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
    factorViewModel.getFactor()
        .observe(viewLifecycleOwner, Observer {
          when (it) {
            is Factor -> showFactor(it.factor)
            is FactorError -> it.exception.showError(content)
          }
        })
    challengesViewModel.getChallenges()
        .observe(viewLifecycleOwner, Observer {
          when (it) {
            is ChallengeList -> showChallenges(it.challenges)
            is ChallengesError -> it.exception.showError(content)
          }
        })
    factorViewModel.loadFactor(factorSid)
    challengesViewModel.loadChallenges(factorSid)
  }

  private fun showFactor(factor: com.twilio.verify.models.Factor) {
    factorInfo.text = factor.string()
    factorInfo.setTextIsSelectable(true)
  }

  private fun showChallenges(challenges: List<Challenge>) {
    viewAdapter = ChallengesAdapter(challenges) {
      val bundle = bundleOf(
          ARG_FACTOR_SID to it.factorSid,
          ARG_CHALLENGE_SID to it.sid
      )
      findNavController().navigate(R.id.action_show_challenge, bundle)
    }
    this.challenges.apply {
      setHasFixedSize(true)
      layoutManager = viewManager
      adapter = viewAdapter
    }
  }
}
