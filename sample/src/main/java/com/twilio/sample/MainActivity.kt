package com.twilio.sample

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Denied
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.sample.R
import kotlinx.android.synthetic.main.activity_main.approveChallenge
import kotlinx.android.synthetic.main.activity_main.challengeActionsGroup
import kotlinx.android.synthetic.main.activity_main.challengeGroup
import kotlinx.android.synthetic.main.activity_main.challengeInfo
import kotlinx.android.synthetic.main.activity_main.createFactor
import kotlinx.android.synthetic.main.activity_main.denyChallenge
import kotlinx.android.synthetic.main.activity_main.factorGroup
import kotlinx.android.synthetic.main.activity_main.factorInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat.MEDIUM

class MainActivity : AppCompatActivity() {

  private lateinit var token: String
  private lateinit var twilioVerifyAdapter: TwilioVerifyAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    twilioVerifyAdapter = TwilioVerifyProvider.instance(applicationContext)
    getPushToken()
    subscribeToEvents()
    createFactor.setOnClickListener {
      startCreateFactor()
    }
  }

  private fun startCreateFactor() {
    val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZ" +
        "jI0YjZmYi0xNTc1NjAzNzE4IiwiaXNzIjoiZWI4MjEyZGZjOTUzMzliMmNmYjIyNThjM2YyNGI2ZmIiLCJuYmYiO" +
        "jE1ODMzNTU2MTcsImV4cCI6MzE2NjcxMTIzNCwic3ViIjoiQUM1MTNhZjAzZjMyMjhmMWU4NTU4Y2ViYmEwMWRjM" +
        "GIzZSIsImdyYW50cyI6eyJhdXRoeSI6eyJzZXJ2aWNlX3NpZCI6IklTMGZjNzA0YmQ3MzY2YWQwZWFjZjJmM2Q1Z" +
        "DE3YmFlN2EiLCJlbnRpdHlfaWQiOiJZRTJmZWI3OTc0YzE3ZWI3ODBiYTM3OGE3NTZhYmE4ZTlmIiwiZmFjdG9yI" +
        "joicHVzaCJ9fX0.ZxnVDKL56vSHNKd-3AUrc7Zx4N88HEk-eTWRD7gJOBk"
    val name = "name"
    factorGroup.visibility = VISIBLE
    factorInfo.text = "Creating factor"
    if (!this::token.isInitialized) {
      showError(IllegalArgumentException("Invalid push token"))
    } else {
      twilioVerifyAdapter.createFactor(
          PushFactorInput(name, token, jwt),
          ::onSuccess,
          ::showError
      )
    }
  }

  private fun onSuccess(factor: Factor) {
    val info = "Factor sid:\n${factor.sid}\nStatus: ${factor.status}"
    factorInfo.text = info
    challengeGroup.visibility = GONE
  }

  private fun getPushToken() {
    FirebaseInstanceId.getInstance()
        .instanceId.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        task.exception?.let { ::showError }
        return@OnCompleteListener
      }
      task.result?.token?.let {
        token = it
      }
    })
  }

  private fun subscribeToEvents() {
    CoroutineScope(Dispatchers.Main).launch {
      VerifyEventBus.consumeEvent<NewChallenge> {
        showChallenge(it.challenge)
      }
    }
  }

  private fun showChallenge(challenge: Challenge) {
    challengeGroup.visibility = VISIBLE
    val info = "${challenge.challengeDetails.message}\nStatus: ${challenge.status.value}\n" +
        "${challenge.challengeDetails.toString(this)}Sid:\n ${challenge.sid}\n" +
        "Expire on: ${DateUtils.formatSameDayTime(
            challenge.expirationDate.time, System.currentTimeMillis(), MEDIUM, MEDIUM
        )}"
    challengeInfo.text = info
    challengeActionsGroup.visibility = if (challenge.status == Pending) VISIBLE else GONE
    approveChallenge.setOnClickListener {
      updateChallenge(challenge, Approved)
    }
    denyChallenge.setOnClickListener {
      updateChallenge(challenge, Denied)
    }
  }

  private fun updateChallenge(
    challenge: Challenge,
    status: ChallengeStatus
  ) {
    twilioVerifyAdapter.updateChallenge(
        UpdatePushChallengeInput(challenge.factorSid, challenge.sid, status),
        { getChallenge(challenge.sid, challenge.factorSid) },
        ::showError
    )
  }

  private fun getChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    twilioVerifyAdapter.getChallenge(
        challengeSid,
        factorSid,
        ::showChallenge,
        ::showError
    )
  }

  private fun showError(e: Exception) {
    e.printStackTrace()
  }
}

private fun ChallengeDetails.toString(context: Context): String {
  return takeIf { it.date != null || it.fields.isNotEmpty() }?.let {
    "Details:\n${fields.joinToString(
        "\n"
    ) {
      "  ${it.label} = ${it.value}"
    }}${(date?.let {
      "  Date = ${DateUtils.formatDateTime(context, it.time, 0)}"
    } ?: "")}\n"
  } ?: ""
}
