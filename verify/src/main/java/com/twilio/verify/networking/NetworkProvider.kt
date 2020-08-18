package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface NetworkProvider {
  fun execute(
    request: Request,
    success: (response: Response) -> Unit,
    error: (NetworkException) -> Unit
  )
}

class NetworkException constructor(
  message: String?,
  cause: Throwable?,
  val failureResponse: FailureResponse?
) : Exception(message, cause) {
  constructor(
    failureResponse: FailureResponse?
  ) : this(
      "Network exception with status code ${failureResponse?.responseCode} and body: ${failureResponse?.errorBody}",
      null,
      failureResponse
  )

  constructor(cause: Throwable) : this(
      cause.message, cause, null
  )
}

class FailureResponse(
  val responseCode: Int,
  val errorBody: String?,
  val headers: Map<String, List<String>>?
)