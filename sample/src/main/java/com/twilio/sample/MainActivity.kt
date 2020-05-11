package com.twilio.sample

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.sample.model.CreateFactorData
import com.twilio.sample.push.NewChallenge
import com.twilio.sample.push.VerifyEventBus
import com.twilio.sample.storage.LocalStorage
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Denied
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.sample.R
import kotlinx.android.synthetic.main.activity_main.approveChallenge
import kotlinx.android.synthetic.main.activity_main.challengeActionsGroup
import kotlinx.android.synthetic.main.activity_main.challengeGroup
import kotlinx.android.synthetic.main.activity_main.challengeInfo
import kotlinx.android.synthetic.main.activity_main.content
import kotlinx.android.synthetic.main.activity_main.createFactor
import kotlinx.android.synthetic.main.activity_main.denyChallenge
import kotlinx.android.synthetic.main.activity_main.factorGroup
import kotlinx.android.synthetic.main.activity_main.factorInfo
import kotlinx.android.synthetic.main.activity_main.identityInput
import kotlinx.android.synthetic.main.activity_main.jwtUrlInput
import java.text.DateFormat.MEDIUM

class MainActivity : AppCompatActivity() {

  private lateinit var token: String
  private lateinit var twilioVerifyAdapter: TwilioVerifyAdapter
  private val storage: LocalStorage by lazy {
    LocalStorage(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    getPushToken()
    subscribeToEvents()
    jwtUrlInput.setText(storage.jwtURL)
    createFactor.setOnClickListener {
      startCreateFactor()
    }
  }

  private fun startCreateFactor() {
    hideKeyboardFrom()
    when {
      !this::token.isInitialized -> showError(IllegalArgumentException("Invalid push token"))
      jwtUrlInput.text.toString().isEmpty() -> showError(
          IllegalArgumentException("Invalid jwt url")
      )
      identityInput.text.toString().isEmpty() -> showError(
          IllegalArgumentException("Invalid entity identity")
      )
      else -> {
        storage.jwtURL = jwtUrlInput.text.toString()
        twilioVerifyAdapter =
          TwilioVerifyProvider.instance(applicationContext, storage.jwtURL)
        createFactor(storage.jwtURL, identityInput.text.toString())
      }
    }
  }

  private fun createFactor(
    jwtUrl: String,
    identity: String
  ) {
    factorGroup.visibility = VISIBLE
    challengeGroup.visibility = GONE
    challengeActionsGroup.visibility = GONE
    factorInfo.text = "Creating factor"
    val createFactorData = CreateFactorData(
        jwtUrl, identity, "$identity's factor", token
    )
    twilioVerifyAdapter.createFactor(
        createFactorData, ::onSuccess
    ) {
      showError(it)
      factorGroup.visibility = GONE
    }
  }

  private fun onSuccess(factor: Factor) {
    val info = "Factor sid:\n${factor.sid}\nStatus: ${factor.status}"
    factorInfo.text = info
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
    VerifyEventBus.consumeEvent<NewChallenge> {
      showChallenge(it.challenge)
    }
  }

  private fun showChallenge(challenge: Challenge) {
    challengeGroup.visibility = VISIBLE
    challengeActionsGroup.visibility = if (challenge.status == Pending) VISIBLE else GONE
    val info = "${challenge.challengeDetails.message}\nStatus: ${challenge.status.value}\n" +
        "${challenge.challengeDetails.toString(this)}Sid:\n ${challenge.sid}\n" +
        "Expire on: ${DateUtils.formatSameDayTime(
            challenge.expirationDate.time, System.currentTimeMillis(), MEDIUM, MEDIUM
        )}\n" +
        "Updated at: ${DateUtils.formatSameDayTime(
            challenge.updatedAt.time, System.currentTimeMillis(), MEDIUM, MEDIUM
        )}"
    challengeInfo.text = info
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
    Snackbar.make(content, e.message.toString(), LENGTH_LONG)
        .show()
  }

  fun hideKeyboardFrom() {
    val imm: InputMethodManager =
      getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(content.windowToken, 0)
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
