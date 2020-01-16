package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
enum class MediaTypeValue(val type: String) {
  UrlEncoded("application/x-www-form-urlencoded"),
  Json("application/json")
}

enum class MediaTypeHeader(val type: String) {
  ContentType("Content-Type"),
  Accept("Accept")
}