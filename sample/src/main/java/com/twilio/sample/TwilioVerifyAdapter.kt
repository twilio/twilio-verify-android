/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample

import android.content.Context
import com.twilio.sample.networking.OkHttpProvider
import com.twilio.sample.networking.okHttpClient
import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.networking.BasicAuthorization
import com.twilio.verify.sample.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TwilioVerifyAdapter(
  private val applicationContext: Context,
  private val okHttpClient: OkHttpClient = okHttpClient(),
  private val twilioVerify: TwilioVerify = TwilioVerify.Builder(
      applicationContext, BasicAuthorization(BuildConfig.ACCOUNT_SID, BuildConfig.AUTH_TOKEN)
  ).networkProvider(OkHttpProvider(okHttpClient)).build(),
  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TwilioVerify by twilioVerify {

  fun createFactor(
    jwtUrl: String,
    identity: String,
    factorName: String,
    pushToken: String,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    CoroutineScope(mainDispatcher).launch {
      try {
        val jwt = getJwt(jwtUrl, identity)
        val factor = createFactor(PushFactorInput(factorName, pushToken, jwt))
        onFactorCreated(factor, success)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
  }

  fun verifyFactor(verifyFactorInput: VerifyFactorInput) {
    twilioVerify.verifyFactor(verifyFactorInput, { factor ->
      VerifyEventBus.send(VerifiedFactor(factor))
    }, ::handleError)
  }

  fun getChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    twilioVerify.getChallenge(challengeSid, factorSid, { challenge ->
      VerifyEventBus.send(NewChallenge(challenge))
    }, ::handleError)
  }

  private fun onFactorCreated(
    factor: Factor,
    success: (Factor) -> Unit
  ) {
    success(factor)
    waitForFactorVerified(factor, success)
  }

  private fun waitForFactorVerified(
    factor: Factor,
    onFactorVerified: (Factor) -> Unit
  ) {
    CoroutineScope(mainDispatcher).launch {
      VerifyEventBus.consumeEvent<VerifiedFactor> { event ->
        if (event.factor.sid == factor.sid) {
          onFactorVerified(event.factor)
        }
      }
    }
  }

  private fun handleError(exception: TwilioVerifyException) {
    exception.printStackTrace()
  }

  private suspend fun createFactor(factorInput: FactorInput) = withContext(dispatcher) {
    return@withContext suspendCancellableCoroutine<Factor> { cont ->
      twilioVerify.createFactor(factorInput, { factor ->
        cont.resume(factor)
      }, { exception ->
        cont.resumeWithException(exception)
      })
    }
  }

  private suspend fun getJwt(
    url: String,
    identity: String
  ): String = withContext(dispatcher) {
    return@withContext suspendCancellableCoroutine<String> { cont ->
      val request = Request.Builder()
          .url("$url/auth")
          .post(FormBody.Builder().add("identity", identity).build())
          .build()
      okHttpClient.newCall(request)
          .enqueue(object : Callback {
            override fun onFailure(
              call: Call,
              e: IOException
            ) {
              cont.resumeWithException(e)
            }

            override fun onResponse(
              call: Call,
              response: Response
            ) {
              response.takeIf { it.isSuccessful }
                  ?.body?.string()
                  ?.let { JSONObject(it) }
                  ?.getString("token")
                  ?.let { cont.resume(it) }
            }

          })
    }
  }
}

object TwilioVerifyProvider {
  private lateinit var twilioVerify: TwilioVerifyAdapter

  fun instance(applicationContext: Context): TwilioVerifyAdapter {
    if (!this::twilioVerify.isInitialized) {
      twilioVerify = TwilioVerifyAdapter(applicationContext)
    }
    return twilioVerify
  }
}