package com.twilio.verify.api

import com.twilio.verify.BuildConfig
import com.twilio.verify.data.DateProvider
import java.net.URL

/*
 * Copyright (c) 2020, Twilio Inc.
 */

open class BaseAPIClient(private val dateProvider: DateProvider) {
  fun syncTime(
    success: () -> Unit
  ) {
    dateProvider.syncTime(URL(BuildConfig.BASE_URL), success)
  }
}