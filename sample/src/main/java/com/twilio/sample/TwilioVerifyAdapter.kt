/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample

import android.content.Context
import com.twilio.sample.networking.OkHttpProvider
import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorInput
import com.twilio.verify.models.VerifyFactorInput
import com.twilio.verify.networking.Authorization
import com.twilio.verify.sample.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TwilioVerifyAdapter(
  private val applicationContext: Context,
  private val twilioVerify: TwilioVerify = TwilioVerify.Builder(
      applicationContext, Authorization(BuildConfig.ACCOUNT_SID, BuildConfig.AUTH_TOKEN)
  ).networkProvider(OkHttpProvider()).build(),
  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TwilioVerify by twilioVerify {

  override fun createFactor(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    /**
     * To use coroutines, you can call [TwilioVerifyAdapter.createFactorUsingCoroutine] instead
     * */
    twilioVerify.createFactor(
        factorInput, { onFactorCreated(it, success) }, error
    )
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

  // Using coroutines to create a factor

  private fun createFactorUsingCoroutine(
    factorInput: FactorInput,
    success: (Factor) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    CoroutineScope(mainDispatcher).launch {
      try {
        val factor = createFactor(factorInput)
        onFactorCreated(factor, success)
      } catch (e: TwilioVerifyException) {
        error(e)
      }
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