package com.twilio.verify.api

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateProvider
import com.twilio.verify.networking.NetworkException

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val unauthorized = 401
internal const val dateHeaderKey = "Date"

internal open class BaseAPIClient(private val dateProvider: DateProvider) {

  private var retryCount = 0

  fun validateException(
    exception: NetworkException,
    retry: () -> Unit,
    error: (TwilioVerifyException) -> Unit
  ) {
    if (shouldSyncTimeRetry()) {
      when (exception.failureResponse?.responseCode) {
        unauthorized -> exception.failureResponse.headers?.get(dateHeaderKey)
            ?.first()
            ?.let { date ->
              syncTime(date)
              retryCount++
              retry()
            } ?: error(TwilioVerifyException(exception, NetworkError))
        else -> error(TwilioVerifyException(exception, NetworkError))
      }
    } else {
      retryCount = 0
      error(TwilioVerifyException(exception, NetworkError))
    }
  }

  private fun shouldSyncTimeRetry() = retryCount < 1

  private fun syncTime(
    date: String
  ) {
    dateProvider.syncTime(date)
  }
}