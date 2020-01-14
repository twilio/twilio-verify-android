package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
enum class MediaType(val type: String) {
  UrlEncoded("x-www-form-urlencoded"),
  FormData("form-data")
}