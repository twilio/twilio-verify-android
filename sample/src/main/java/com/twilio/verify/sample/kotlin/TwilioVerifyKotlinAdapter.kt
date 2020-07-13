/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.kotlin

import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorPayload
import com.twilio.verify.models.FactorType.PUSH
import com.twilio.verify.models.PushFactorPayload
import com.twilio.verify.models.UpdatePushFactorPayload
import com.twilio.verify.models.VerifyPushFactorPayload
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.model.EnrollmentResponse
import com.twilio.verify.sample.model.getFactorType
import com.twilio.verify.sample.networking.SampleBackendAPIClient
import com.twilio.verify.sample.networking.getEnrollmentResponse
import com.twilio.verify.sample.push.NewChallenge
import com.twilio.verify.sample.push.VerifyEventBus

class TwilioVerifyKotlinAdapter(
  private val twilioVerify: TwilioVerify,
  private val verifyEventBus: VerifyEventBus = VerifyEventBus
) : TwilioVerify by twilioVerify, TwilioVerifyAdapter {

  override fun createFactor(
    createFactorData: CreateFactorData,
    sampleBackendAPIClient: SampleBackendAPIClient,
    success: (Factor) -> Unit,
    error: (Throwable) -> Unit
  ) {
    try {
      sampleBackendAPIClient.getEnrollmentResponse(
          createFactorData.identity, createFactorData.enrollmentUrl, { enrollmentResponse ->
        val factorPayload = getFactorPayload(createFactorData, enrollmentResponse)
        twilioVerify.createFactor(factorPayload, { factor ->
          verifyFactor(factor, success, error)
        }, error)
      }, error
      )
    } catch (e: TwilioVerifyException) {
      error(e)
    } catch (e: Exception) {
      error(e)
    }
  }

  private fun verifyFactor(
    factor: Factor,
    success: (Factor) -> Unit,
    error: (Exception) -> Unit
  ) {
    when (factor.type) {
      PUSH -> twilioVerify.verifyFactor(
          VerifyPushFactorPayload(factor.sid),
          success,
          error
      )
    }
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
    twilioVerify.updateFactor(UpdatePushFactorPayload(factor.sid, token), {}, ::handleError)
  }

  override fun showChallenge(
    challengeSid: String,
    factorSid: String
  ) {
    verifyEventBus.send(NewChallenge(challengeSid, factorSid))
  }

  private fun getFactorPayload(
    createFactorData: CreateFactorData,
    enrollmentResponse: EnrollmentResponse
  ): FactorPayload {
    return when (enrollmentResponse.getFactorType()) {
      PUSH -> PushFactorPayload(
          createFactorData.factorName, enrollmentResponse.serviceSid,
          enrollmentResponse.identity, createFactorData.pushToken, enrollmentResponse.token
      )
      else -> throw IllegalStateException("Unexpected value: " + enrollmentResponse.factorType)
    }
  }

  private fun handleError(exception: TwilioVerifyException) {
    exception.printStackTrace()
  }
}

