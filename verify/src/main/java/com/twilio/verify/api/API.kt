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
  READ("read")
}

fun generateToken(
  authentication: Authentication,
  identity: String,
  factorSid: String? = null,
  challengeSid: String? = null,
  serviceSid: String,
  action: Action,
  success: (authToken: String) -> Unit,
  error: (TwilioVerifyException) -> Unit
) {
  authentication.generateJWE(
      identity = identity, factorSid = factorSid,
      challengeSid = challengeSid, serviceSid = serviceSid, action = action,
      success = { authToken ->
        success(authToken)
      }, error = { exception ->
    error(
        TwilioVerifyException(
            AuthenticationTokenException(exception), AuthenticationTokenError
        )
    )
  })
}

class AuthenticationTokenException(exception: Exception) : Exception(exception)