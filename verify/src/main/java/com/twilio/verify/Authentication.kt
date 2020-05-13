package com.twilio.verify

import com.twilio.verify.api.Action

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface Authentication {
  fun generateJWE(
    identity: String,
    factorSid: String? = null,
    challengeSid: String? = null,
    serviceSid: String,
    action: Action,
    success: (token: String) -> Unit,
    error: (Exception) -> Unit
  )
}