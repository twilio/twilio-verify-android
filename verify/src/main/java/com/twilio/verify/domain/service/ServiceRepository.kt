package com.twilio.verify.domain.service

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.api.ServiceAPIClient
import com.twilio.verify.models.Factor
import com.twilio.verify.models.Service
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal class ServiceRepository(
  private val apiClient: ServiceAPIClient,
  private val serviceMapper: ServiceMapper = ServiceMapper()
) : ServiceProvider {

  override fun get(
    serviceSid: String,
    factor: Factor,
    success: (Service) -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    fun toService(response: JSONObject) {
      try {
        success(serviceMapper.fromApi(response))
      } catch (e: TwilioVerifyException) {
        error(e)
      }
    }
    apiClient.get(serviceSid, factor, ::toService, error)
  }
}