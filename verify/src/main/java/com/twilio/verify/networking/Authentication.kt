package com.twilio.verify.networking

import com.twilio.verify.models.Factor

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal interface Authentication {
  fun generateJWT(
    factor: Factor
  ): String
}