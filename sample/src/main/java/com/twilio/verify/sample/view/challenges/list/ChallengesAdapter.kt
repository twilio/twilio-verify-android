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

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.twilio.verify.models.Challenge
import com.twilio.verify.sample.R.id
import com.twilio.verify.sample.R.layout

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
    private var challengeSid: TextView? = null
    private var challengeMessage: TextView? = null
    private var challengeStatus: TextView? = null
    private var challengeCreatedAt: TextView? = null
    private var challengeExpireOn: TextView? = null

    init {
      challengeSid = itemView.findViewById(
        id.challengeSidText
      )
      challengeMessage = itemView.findViewById(
        id.challengeMessageText
      )
      challengeStatus = itemView.findViewById(
        id.challengeStatusText
      )
      challengeCreatedAt = itemView.findViewById(
        id.challengeCreatedAtText
      )
      challengeExpireOn = itemView.findViewById(
        id.challengeExpireOnText
      )
    }

    fun bind(
      challenge: Challenge,
      onChallengeClick: (Challenge) -> Unit
    ) {
      challengeSid?.text = challenge.sid
      challengeMessage?.text = challenge.challengeDetails.message
      challengeStatus?.text = challenge.status.value
      challengeCreatedAt?.text = DateUtils.formatDateTime(
        itemView.context,
        challenge.createdAt.time,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
      )
      challengeExpireOn?.text = DateUtils.formatDateTime(
        itemView.context,
        challenge.expirationDate.time,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
      )
      itemView.setOnClickListener {
        onChallengeClick(challenge)
      }
    }
  }
}
