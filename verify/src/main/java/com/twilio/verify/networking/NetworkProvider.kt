package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface NetworkProvider {
  fun execute(
    request: Request,
    success: (response: String) -> Unit,
    failure: (exception: Exception) -> Unit
  )
}