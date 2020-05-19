package com.twilio.verify.sample.view.factors.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.twilio.verify.models.Factor
import com.twilio.verify.sample.R.id
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.view.factors.list.FactorsAdapter.FactorViewHolder
import com.twilio.verify.sample.view.string

class FactorsAdapter(
  private val factors: List<Factor>,
  private val onFactorClick: (Factor) -> Unit
) :
    Adapter<FactorViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): FactorViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return FactorViewHolder(
        inflater.inflate(layout.view_factor, parent, false)
    )
  }

  override fun onBindViewHolder(
    holder: FactorViewHolder,
    position: Int
  ) {
    val factor = factors[position]
    holder.bind(factor, onFactorClick)
  }

  override fun getItemCount(): Int = factors.size

  fun getItemSid(position: Int): String = factors[position].sid

  class FactorViewHolder(view: View) : ViewHolder(view) {
    private var factorInfo: TextView? = null

    init {
      factorInfo = itemView.findViewById(
          id.factorInfo
      )
    }

    fun bind(
      factor: Factor,
      onFactorClick: (Factor) -> Unit
    ) {
      factorInfo?.text = factor.string()
      itemView.setOnClickListener {
        onFactorClick(factor)
      }
    }
  }
}