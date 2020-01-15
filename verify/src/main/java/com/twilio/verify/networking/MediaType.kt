package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
enum class MediaType(val type: String) {
  UrlEncoded("application/x-www-form-urlencoded"),
  Json("application/json"),
  ContentType("Content-Type"),
  Accept("Accept")
}