package com.twilio.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.verify.TwilioVerify
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.sample.R
import kotlinx.android.synthetic.main.activity_main.createFactor
import kotlinx.android.synthetic.main.activity_main.createFactorCoroutines
import kotlinx.android.synthetic.main.activity_main.result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {

  private lateinit var token: String
  private lateinit var twilioVerify: TwilioVerify

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    twilioVerify = TwilioVerify.Builder(applicationContext, Authorization("test", "test"))
        .build()

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
    val jwt =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
          "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
          "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
          "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
          "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
          "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val name = "name"
    createFactor.setOnClickListener {
      result.text = "Creating factor"
      if (!this::token.isInitialized) {
        result.text = "Invalid push token"
      } else {
        createFactor(jwt, name)
      }
    }
    createFactorCoroutines.setOnClickListener {
      result.text = "Creating factor"
      if (!this::token.isInitialized) {
        result.text = "Invalid push token"
      } else {
        createFactorUsingCoroutine(jwt, name)
      }
    }
  }

  private fun createFactor(
    jwt: String,
    name: String
  ) {
    twilioVerify.createFactor(
        PushFactorInput(name, token, jwt),
        { factor -> result.text = factor.sid },
        { e ->
          e.printStackTrace()
          result.text = e.message
        })
  }

  private fun createFactorUsingCoroutine(
    jwt: String,
    name: String
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      try {
        val factor = createFactor(name, token, jwt)
        result.text = factor.sid
      } catch (e: Exception) {
        e.printStackTrace()
        result.text = e.message
      }
    }
  }

  private suspend fun createFactor(
    friendlyName: String,
    pushToken: String,
    jwt: String
  ): Factor = withContext(Dispatchers.IO) {
    return@withContext suspendCancellableCoroutine<Factor> { cont ->
      twilioVerify.createFactor(PushFactorInput(friendlyName, pushToken, jwt), { factor ->
        cont.resume(factor)
      }, { exception ->
        cont.resumeWithException(exception)
      })
    }
  }
}
