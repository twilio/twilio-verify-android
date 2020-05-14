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
  serviceSid: String,
  identity: String,
  factorSid: String? = null,
  challengeSid: String? = null,
  action: Action,
  success: (authToken: String) -> Unit,
  error: (TwilioVerifyException) -> Unit
) {
  authentication.generateJWE(
      serviceSid = serviceSid, identity = identity, factorSid = factorSid,
      challengeSid = challengeSid, action = action,
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