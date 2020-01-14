package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
enum class HttpMethod(method: String) {
  Get("GET"),
  Post("POST"),
  Delete("DELETE"),
  Put("PUT")
}