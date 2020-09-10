package com.twilio.verify.networking

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Factor

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal interface Authentication {
  @Throws(TwilioVerifyException::class)
  fun generateJWT(
    factor: Factor
  ): String
}
