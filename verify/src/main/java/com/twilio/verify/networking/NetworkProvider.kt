package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface NetworkProvider {
  fun execute(
    request: Request,
    success: (response: Response) -> Unit,
    syncTime: ((date: String) -> Unit)? = null,
    error: (NetworkException) -> Unit
  )
}

class NetworkException constructor(
  message: String?,
  cause: Throwable?
) : Exception(message, cause) {
  constructor(
    statusCode: Int,
    errorResponse: String?
  ) : this("Network exception with status code $statusCode and body: $errorResponse", null)

  constructor(cause: Throwable) : this(
      cause.message, cause
  )
}