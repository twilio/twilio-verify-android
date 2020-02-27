package com.twilio.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.sample.R
import kotlinx.android.synthetic.main.activity_main.createFactor
import kotlinx.android.synthetic.main.activity_main.createFactorCoroutines
import kotlinx.android.synthetic.main.activity_main.result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {

  private lateinit var token: String
  private lateinit var twilioVerifyAdapter: TwilioVerifyAdapter
  private lateinit var subscription: ReceiveChannel<VerifiedFactor>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    twilioVerifyAdapter = TwilioVerifyAdapter(applicationContext)
    subscription = EventBus.asChannel()
    FirebaseInstanceId.getInstance()
        .instanceId.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        result.text = task.exception?.message
        return@OnCompleteListener
      }

      // Get new Instance ID token
      task.result?.token?.let {
        token = it
      }
    })
    val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZ" +
        "jI0YjZmYi0xNTc1NjAzNzE4IiwiaXNzIjoiZWI4MjEyZGZjOTUzMzliMmNmYjIyNThjM2YyNGI2ZmIiLCJuYmYiO" +
        "jE1ODI3NTkyNTAsImV4cCI6MzE2NTUxODUwMCwic3ViIjoiQUM1MTNhZjAzZjMyMjhmMWU4NTU4Y2ViYmEwMWRjM" +
        "GIzZSIsImdyYW50cyI6eyJhdXRoeSI6eyJzZXJ2aWNlX3NpZCI6IklTYmI5ZTcxMTUxM2IzMDNkMmU5MzM0NDQ1O" +
        "DQ4ZjcyYmEiLCJlbnRpdHlfaWQiOiJZRWQ3ZmJmODRhMTExNjRkMDM2YmM1NjBlMTFjMmE5NjJjIiwiZmFjdG9yI" +
        "joicHVzaCJ9fX0.-E3toPdlHmCOman0OfBp5qhPxwFvbim2q7dl1xQcjHc"
    val name = "name"
    createFactor.setOnClickListener {
      startCreateFactor { createFactor(jwt, name) }
    }
    createFactorCoroutines.setOnClickListener {
      startCreateFactor { createFactorUsingCoroutine(jwt, name) }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    subscription.cancel()
  }

  private fun startCreateFactor(createFactorMethod: () -> Unit) {
    result.text = "Creating factor"
    if (!this::token.isInitialized) {
      result.text = "Invalid push token"
    } else {
      createFactorMethod()
    }
  }

  private fun createFactor(
    jwt: String,
    name: String
  ) {
    twilioVerifyAdapter.createFactor(PushFactorInput(name, token, jwt), ::onSuccess, ::onError)
  }

  private fun createFactorUsingCoroutine(
    jwt: String,
    name: String
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      try {
        val factor = createFactor(name, token, jwt)
        onSuccess(factor)
        subscription.consumeEach { event ->
          if (event.factorSid == factor.sid) {
            onSuccess(factor)
          }
        }
      } catch (e: Exception) {
        onError(e)
      }
    }
  }

  private suspend fun createFactor(
    friendlyName: String,
    pushToken: String,
    jwt: String
  ): Factor = withContext(Dispatchers.IO) {
    return@withContext suspendCancellableCoroutine<Factor> { cont ->
      twilioVerifyAdapter.createFactor(PushFactorInput(friendlyName, pushToken, jwt), { factor ->
        cont.resume(factor)
      }, { exception ->
        cont.resumeWithException(exception)
      })
    }
  }

  private fun onSuccess(factor: Factor) {
    result.text = "Factor sid: ${factor.sid}\nStatus: ${factor.status}"
  }

  private fun onError(exception: Exception) {
    exception.printStackTrace()
    result.text = exception.message
  }
}
