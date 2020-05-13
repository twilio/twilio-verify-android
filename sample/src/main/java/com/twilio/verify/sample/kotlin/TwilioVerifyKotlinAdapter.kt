/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.kotlin

import android.content.Context
import com.twilio.verify.Authentication
import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.FactorType.PUSH
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.UpdatePushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.model.EnrollmentResponse
import com.twilio.verify.sample.networking.OkHttpProvider
import com.twilio.verify.sample.networking.SampleBackendAPIClient
import com.twilio.verify.sample.networking.okHttpClient
import com.twilio.verify.sample.push.NewChallenge
import com.twilio.verify.sample.push.VerifyEventBus
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
  authentication: Authentication,
  private val twilioVerify: TwilioVerify = TwilioVerify.Builder(
      applicationContext, authentication
  )
      .networkProvider(OkHttpProvider(okHttpClient))
      .build(),
  private val sampleBackendAPIClient: SampleBackendAPIClient = SampleBackendAPIClient(okHttpClient),
  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val verifyEventBus: VerifyEventBus = VerifyEventBus
) : TwilioVerify by twilioVerify, TwilioVerifyAdapter {

  override fun createFactor(
    createFactorData: CreateFactorData,
    success: (Factor) -> Unit,
    error: (Exception) -> Unit
  ) {
    CoroutineScope(mainDispatcher).launch {
      try {
        val enrollmentResponse =
          sampleBackendAPIClient.enrollment(
              createFactorData.identity
          )
        val factorInput = getFactorInput(createFactorData, enrollmentResponse)
        val factor = createFactor(factorInput)
        onFactorCreated(factor, success, error)
      } catch (e: TwilioVerifyException) {
        error(e)
      } catch (e: Exception) {
        error(e)
      }
    }
  }

  private fun getFactorInput(
    createFactorData: CreateFactorData,
    enrollmentResponse: EnrollmentResponse
  ): FactorInput {
    return when (enrollmentResponse.factorType) {
      PUSH -> PushFactorInput(
          createFactorData.factorName, enrollmentResponse.serviceSid,
          enrollmentResponse.identity, createFactorData.pushToken, enrollmentResponse.token
      )
    }
  }

  override fun showChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    verifyEventBus.send(NewChallenge(challengeSid, factorSid))
  }

  override fun getFactors(
    success: (List<Factor>) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    twilioVerify.getAllFactors(success, error)
  }

  override fun updatePushToken(token: String) {
    twilioVerify.getAllFactors({ factors ->
      for (factor in factors) {
        updateFactor(factor, token)
      }
    }, ::handleError)
  }

  private fun updateFactor(
    factor: Factor,
    token: String
  ) {
    twilioVerify.updateFactor(UpdatePushFactorInput(factor.sid, token), {}, ::handleError)
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

  private suspend fun createFactor(factorInput: FactorInput) = withContext(dispatcher) {
    return@withContext suspendCancellableCoroutine<Factor> { cont ->
      twilioVerify.createFactor(factorInput, { factor ->
        cont.resume(factor)
      }, { exception ->
        cont.resumeWithException(exception)
      })
    }
  }

  private fun handleError(exception: TwilioVerifyException) {
    exception.printStackTrace()
  }
}

