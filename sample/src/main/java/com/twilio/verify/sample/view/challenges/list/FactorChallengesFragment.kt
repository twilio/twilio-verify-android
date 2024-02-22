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

package com.twilio.verify.sample.view.challenges.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twilio.verify.models.Challenge
import com.twilio.verify.sample.R
import com.twilio.verify.sample.databinding.FragmentFactorChallengesBinding
import com.twilio.verify.sample.model.AppModel
import com.twilio.verify.sample.view.challenges.update.ARG_CHALLENGE_SID
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.viewmodel.ChallengeList
import com.twilio.verify.sample.viewmodel.ChallengesError
import com.twilio.verify.sample.viewmodel.ChallengesViewModel
import com.twilio.verify.sample.viewmodel.Factor
import com.twilio.verify.sample.viewmodel.FactorError
import com.twilio.verify.sample.viewmodel.FactorViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FactorChallengesFragment : Fragment() {
  private lateinit var sid: String
  private lateinit var viewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewManager: LinearLayoutManager
  private val factorViewModel: FactorViewModel by activityViewModel()
  private val challengesViewModel: ChallengesViewModel by activityViewModel()
  private var _binding: FragmentFactorChallengesBinding? = null
  private val binding get() = _binding!!


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      sid = it.getString(ARG_FACTOR_SID) ?: ""
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentFactorChallengesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewManager = LinearLayoutManager(view?.context)
    factorViewModel.getFactor()
      .observe(
        viewLifecycleOwner,
        Observer {
          when (it) {
            is Factor -> showFactor(it.factor)
            is FactorError -> it.exception.showError(binding.content)
          }
        }
      )
    challengesViewModel.getChallenges()
      .observe(
        viewLifecycleOwner,
        Observer {
          when (it) {
            is ChallengeList -> showChallenges(it.challenges)
            is ChallengesError -> it.exception.showError(binding.content)
          }
        }
      )
    val dividerItemDecoration = DividerItemDecoration(
      binding.challenges.context,
      viewManager.orientation
    )
    binding.challenges.addItemDecoration(dividerItemDecoration)
    factorViewModel.loadFactor(sid)
    challengesViewModel.loadChallenges(sid)
    binding.silentApproveCheck.isChecked = AppModel.silentlyApproveChallengesPerFactor[sid] == true
    binding.silentApproveCheck.setOnCheckedChangeListener { _, isChecked ->
      factorViewModel.changeSilentApproveChallenges(sid, isChecked)
    }
  }

  private fun showFactor(factor: com.twilio.verify.models.Factor) {
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

  private fun showChallenges(challenges: List<Challenge>) {
    viewAdapter = ChallengesAdapter(challenges) {
      val bundle = bundleOf(
        ARG_FACTOR_SID to it.factorSid,
        ARG_CHALLENGE_SID to it.sid
      )
      findNavController().navigate(R.id.action_show_challenge, bundle)
    }
    binding.challenges.apply {
      setHasFixedSize(true)
      layoutManager = viewManager
      adapter = viewAdapter
    }
  }
}
