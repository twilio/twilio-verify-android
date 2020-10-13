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

package com.twilio.verify.data

import android.content.SharedPreferences
import java.text.ParseException
import java.util.concurrent.TimeUnit.MILLISECONDS

internal interface DateProvider {
  fun getCurrentTime(): Long

  @Throws(ParseException::class)
  fun syncTime(
    date: String
  )
}

internal const val timeCorrectionKey = "timeCorrection"

internal class DateAdapter(
  private val preferences: SharedPreferences
) : DateProvider {
  override fun getCurrentTime(): Long {
    val timeDifference = preferences.getLong(
      timeCorrectionKey, 0
    )
    return localTime() + timeDifference
  }

  override fun syncTime(
    date: String
  ) {
    saveTime(fromRFC1123Date(date).time)
  }

  private fun localTime() = MILLISECONDS.toSeconds(System.currentTimeMillis())

  private fun saveTime(time: Long) {
    val timeCorrection =
      MILLISECONDS.toSeconds(time) - localTime()
    preferences.edit()
      .putLong(timeCorrectionKey, timeCorrection)
      .apply()
  }
}
