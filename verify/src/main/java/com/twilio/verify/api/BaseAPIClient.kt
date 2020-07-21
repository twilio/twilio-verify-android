package com.twilio.verify.api

import com.twilio.verify.data.DateProvider

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal open class BaseAPIClient(private val dateProvider: DateProvider) {
  fun syncTime(
    date: String
  ) {
    dateProvider.syncTime(date)
  }
}