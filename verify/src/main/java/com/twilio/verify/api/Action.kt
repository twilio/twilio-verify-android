package com.twilio.verify.api

import com.twilio.verify.Authentication
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.AuthenticationTokenError

/*
 * Copyright (c) 2020, Twilio Inc.
 */

enum class Action(val value: String) {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  FETCH("fetch"),
  READ("read")
}

fun generateToken(
  authentication: Authentication,
  identity: String,
  factorSid: String? = null,
  challengeSid: String? = null,
  serviceSid: String? = null,
  action: Action,
  success: (authToken: String) -> Unit,
  error: (TwilioVerifyException) -> Unit
) {
  authentication.generateJWE(
      identity = identity, factorSid = factorSid,
      challengeSid = challengeSid, serviceSid = serviceSid, action = action,
      success = { authToken ->
        success(authToken)
      }, error = {
    error(
        TwilioVerifyException(
            AuthenticationTokenException("Invalid token"), AuthenticationTokenError
        )
    )
  })
}

class AuthenticationTokenException(message: String) : Exception(message)