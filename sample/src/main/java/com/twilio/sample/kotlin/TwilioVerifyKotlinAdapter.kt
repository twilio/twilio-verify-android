/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample.kotlin

import android.content.Context
import com.twilio.sample.TwilioVerifyAdapter
import com.twilio.sample.model.CreateFactorData
import com.twilio.sample.networking.OkHttpProvider
import com.twilio.sample.networking.SampleBackendAPIClient
import com.twilio.sample.networking.okHttpClient
import com.twilio.sample.push.NewChallenge
import com.twilio.sample.push.VerifiedFactor
import com.twilio.sample.push.VerifyEventBus
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
import okhttp3.OkHttpClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TwilioVerifyKotlinAdapter(
  applicationContext: Context,
  okHttpClient: OkHttpClient = okHttpClient(),
  private val twilioVerify: TwilioVerify = TwilioVerify.Builder(
      applicationContext, BasicAuthorization(BuildConfig.ACCOUNT_SID, BuildConfig.AUTH_TOKEN)
  ).networkProvider(OkHttpProvider(okHttpClient)).build(),
  private val sampleBackendAPIClient: SampleBackendAPIClient = SampleBackendAPIClient(okHttpClient),
  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TwilioVerify by twilioVerify, TwilioVerifyAdapter {

  override fun createFactor(
    createFactorData: CreateFactorData,
    onSuccess: (Factor) -> Unit,
    onError: (Exception) -> Unit
  ) {
    CoroutineScope(mainDispatcher).launch {
      try {
        val jwt = sampleBackendAPIClient.getJwt(createFactorData.jwtUrl, createFactorData.identity)
        val factor = createFactor(
            PushFactorInput(createFactorData.factorName, createFactorData.pushToken, jwt)
        )
        onFactorCreated(factor, onSuccess)
      } catch (e: TwilioVerifyException) {
        onError(e)
      } catch (e: Exception) {
        onError(e)
      }
    }
  }

  override fun verifyFactor(verifyFactorInput: VerifyFactorInput) {
    twilioVerify.verifyFactor(verifyFactorInput, { factor ->
      VerifyEventBus.send(
          VerifiedFactor(factor)
      )
    }, ::handleError)
  }

  override fun getChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    twilioVerify.getChallenge(challengeSid, factorSid, { challenge ->
      VerifyEventBus.send(
          NewChallenge(challenge)
      )
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
    VerifyEventBus.consumeEvent<VerifiedFactor> { event ->
      if (event.factor.sid == factor.sid) {
        onFactorVerified(event.factor)
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
}

