/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.api

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.data.DateProvider
import com.twilio.verify.networking.NetworkException

internal const val UNAUTHORIZED = 401
internal const val NOT_FOUND = 404
internal const val DATE_HEADER_KEY = "Date"
internal const val RETRY_TIMES = 1

internal open class BaseAPIClient(
  private val dateProvider: DateProvider,
) {
  protected fun validateException(
    exception: NetworkException,
    retryBlock: (Int) -> Unit,
    retries: Int,
    error: (TwilioVerifyException) -> Unit,
  ) {
    if (retries > 0) {
      when (exception.failureResponse?.statusCode) {
        UNAUTHORIZED ->
          exception.failureResponse.headers
            ?.get(DATE_HEADER_KEY)
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

  private fun syncTime(date: String) {
    dateProvider.syncTime(date)
  }
}
