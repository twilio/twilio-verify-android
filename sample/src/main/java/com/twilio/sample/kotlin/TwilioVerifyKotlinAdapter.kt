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
import com.twilio.sample.push.VerifyEventBus
import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.FactorType.PUSH
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
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
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val verifyEventBus: VerifyEventBus = VerifyEventBus
) : TwilioVerify by twilioVerify, TwilioVerifyAdapter {

  override fun createFactor(
    createFactorData: CreateFactorData,
    onSuccess: (Factor) -> Unit,
    onError: (Exception) -> Unit
  ) {
    CoroutineScope(mainDispatcher).launch {
      try {
        val enrollmentResponse =
          sampleBackendAPIClient.enrollment(createFactorData.jwtUrl, createFactorData.identity)
        val factor = createFactor(
            PushFactorInput(
                createFactorData.factorName, enrollmentResponse.serviceSid,
                enrollmentResponse.identity, enrollmentResponse.factorType,
                createFactorData.pushToken, enrollmentResponse.token
            )
        )
        onFactorCreated(factor, onSuccess, onError)
      } catch (e: TwilioVerifyException) {
        onError(e)
      } catch (e: Exception) {
        onError(e)
      }
    }
  }

  override fun getChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    twilioVerify.getChallenge(challengeSid, factorSid, { challenge ->
      verifyEventBus.send(
          NewChallenge(challenge)
      )
    }, ::handleError)
  }

  private fun onFactorCreated(
    factor: Factor,
    onSuccess: (Factor) -> Unit,
    onError: (Exception) -> Unit
  ) {
    when (factor.type) {
      PUSH -> twilioVerify.verifyFactor(VerifyPushFactorInput(factor.sid), onSuccess, onError)
      else -> onSuccess(factor)
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

