package com.twilio.verify

import com.twilio.verify.models.FactorType

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface Authentication {
  fun generateJWE(
    factorType: FactorType,
    factorConfig: Any?,
    identity: String,
    factorSid: String?,
    challengeSid: String?,
    success: (token: String) -> Unit,
    error: (Exception) -> Unit
  )
}