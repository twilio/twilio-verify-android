package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
enum class HttpMethod(val method: String) {
  Get("GET"),
  Post("POST"),
  Delete("DELETE"),
  Put("PUT")
}
