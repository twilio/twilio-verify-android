package com.twilio.verify.domain.service

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor
import com.twilio.verify.models.Service

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal interface ServiceProvider {
  fun get(
    serviceSid: String,
    factor: Factor,
    success: (Service) -> Unit,
    error: (TwilioVerifyException) -> Unit
  )
}