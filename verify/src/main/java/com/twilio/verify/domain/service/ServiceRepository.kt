package com.twilio.verify.domain.service

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.api.ServiceAPIClient
import com.twilio.verify.domain.service.models.Service
import org.json.JSONObject

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal class ServiceRepository(
  private val apiClient: ServiceAPIClient,
  private val serviceMapper: ServiceMapper
) : ServiceProvider {

  override fun get(
    serviceSid: String,
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
    apiClient.get(serviceSid, ::toService, error)
  }
}