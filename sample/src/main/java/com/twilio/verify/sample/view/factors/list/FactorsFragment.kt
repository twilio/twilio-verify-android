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

package com.twilio.verify.sample.view.factors.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.R
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import com.twilio.verify.sample.view.showError
import com.twilio.verify.sample.viewmodel.DeleteFactorError
import com.twilio.verify.sample.viewmodel.FactorList
import com.twilio.verify.sample.viewmodel.FactorsError
import com.twilio.verify.sample.viewmodel.FactorsViewModel
import kotlinx.android.synthetic.main.fragment_factors.content
import kotlinx.android.synthetic.main.fragment_factors.createFactor
import kotlinx.android.synthetic.main.fragment_factors.factors
import org.koin.androidx.viewmodel.ext.android.viewModel

class FactorsFragment : Fragment() {

  private lateinit var viewAdapter: FactorsAdapter
  private val factorsViewModel: FactorsViewModel by viewModel()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_factors, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    createFactor.setOnClickListener { findNavController().navigate(R.id.action_create_factor) }
    factors.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(requireContext())
      ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        ?.let {
          val itemTouchHelper = ItemTouchHelper(
            SwipeToDeleteCallback(
              ::delete, it
            )
          )
          itemTouchHelper.attachToRecyclerView(this)
        }
    }
    factorsViewModel.getFactors()
      .observe(
        viewLifecycleOwner,
        Observer {
          when (it) {
            is FactorList -> showFactors(it.factors)
            is FactorsError -> it.exception.showError(content)
            is DeleteFactorError -> it.exception.showError(content)
          }
        }
      )
    loadFactors()
  }

  private fun delete(position: Int) {
    factorsViewModel.deleteFactor(viewAdapter.getItemSid(position))
  }

  private fun loadFactors() {
    factorsViewModel.loadFactors()
  }

  private fun showFactors(list: List<Factor>) {
    viewAdapter = FactorsAdapter(list) {
      val bundle = bundleOf(
        ARG_FACTOR_SID to it.sid
      )
      findNavController().navigate(R.id.action_show_challenges, bundle)
    }
    factors.apply {
      adapter = viewAdapter
    }
  }
}
