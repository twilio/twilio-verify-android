package com.twilio.verify.api

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateProvider
import com.twilio.verify.networking.NetworkException

/*
 * Copyright (c) 2020, Twilio Inc.
 */

internal const val unauthorized = 401
internal const val notFound = 404
internal const val dateHeaderKey = "Date"
internal const val retryTimes = 1

internal open class BaseAPIClient(private val dateProvider: DateProvider) {

  protected fun validateException(
    exception: NetworkException,
    retryBlock: (Int) -> Unit,
    retries: Int,
    error: (TwilioVerifyException) -> Unit
  ) {
    if (retries > 0) {
      when (exception.failureResponse?.responseCode) {
        unauthorized ->
          exception.failureResponse.headers?.get(dateHeaderKey)
            ?.first()
            ?.let { date ->
              syncTime(date)
              retryBlock(retries - 1)
            } ?: error(TwilioVerifyException(exception, NetworkError))
        else -> error(TwilioVerifyException(exception, NetworkError))
      }
    } else {
      error(TwilioVerifyException(exception, NetworkError))
    }
  }

  private fun syncTime(
    date: String
  ) {
    dateProvider.syncTime(date)
  }
}
