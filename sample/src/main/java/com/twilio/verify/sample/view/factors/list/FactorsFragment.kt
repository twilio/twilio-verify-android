package com.twilio.verify.sample.view.factors.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.R
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.view.challenges.update.ARG_FACTOR_SID
import com.twilio.verify.sample.view.showError
import kotlinx.android.synthetic.main.fragment_factors.content
import kotlinx.android.synthetic.main.fragment_factors.createFactor
import kotlinx.android.synthetic.main.fragment_factors.factors
import org.koin.android.ext.android.inject

class FactorsFragment : Fragment() {

  private lateinit var viewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewManager: RecyclerView.LayoutManager
  private val twilioVerifyAdapter: TwilioVerifyAdapter by inject()

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
    viewManager = LinearLayoutManager(view?.context)
    loadFactors()
  }

  private fun loadFactors() {
    twilioVerifyAdapter.getFactors(::showFactors) { it.showError(content) }
  }

  private fun showFactors(list: List<Factor>) {
    fun delete(position: Int) {
      twilioVerifyAdapter.deleteFactor(list[position].sid, ::loadFactors) {
        it.showError(content)
        loadFactors()
      }
    }

    viewAdapter = FactorsAdapter(list) {
      val bundle = bundleOf(
          ARG_FACTOR_SID to it.sid
      )
      findNavController().navigate(R.id.action_show_challenges, bundle)
    }
    factors.apply {
      setHasFixedSize(true)
      layoutManager = viewManager
      adapter = viewAdapter
      ContextCompat.getDrawable(context, R.drawable.ic_delete)
          ?.let {
            val itemTouchHelper = ItemTouchHelper(
                SwipeToDeleteCallback(
                    ::delete, it
                )
            )
            itemTouchHelper.attachToRecyclerView(this)
          }
    }
  }
}