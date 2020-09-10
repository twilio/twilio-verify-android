package com.twilio.verify.sample.view.challenges.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.twilio.verify.models.Challenge
import com.twilio.verify.sample.R.id
import com.twilio.verify.sample.R.layout
import com.twilio.verify.sample.view.string

class ChallengesAdapter(
  private val challenges: List<Challenge>,
  private val onChallengeClick: (Challenge) -> Unit
) :
  Adapter<ChallengesAdapter.ChallengeViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ChallengeViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return ChallengeViewHolder(
      inflater.inflate(layout.view_challenge, parent, false)
    )
  }

  override fun onBindViewHolder(
    holder: ChallengeViewHolder,
    position: Int
  ) {
    val challenge = challenges[position]
    holder.bind(challenge, onChallengeClick)
  }

  override fun getItemCount(): Int = challenges.size

  class ChallengeViewHolder(view: View) : ViewHolder(view) {
    private var challengeInfo: TextView? = null

    init {
      challengeInfo = itemView.findViewById(
        id.challengeInfo
      )
    }

    fun bind(
      challenge: Challenge,
      onChallengeClick: (Challenge) -> Unit
    ) {
      challengeInfo?.text = challenge.string(itemView.context)
      itemView.setOnClickListener {
        onChallengeClick(challenge)
      }
    }
  }
}
