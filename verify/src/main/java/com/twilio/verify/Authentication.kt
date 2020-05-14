package com.twilio.verify

import com.twilio.verify.api.Action

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface Authentication {
  fun generateJWE(
    serviceSid: String,
    identity: String,
    factorSid: String? = null,
    challengeSid: String? = null,
    action: Action,
    success: (token: String) -> Unit,
    error: (Exception) -> Unit
  )
}