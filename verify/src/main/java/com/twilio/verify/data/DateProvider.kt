package com.twilio.verify.data

import android.content.SharedPreferences
import java.text.ParseException
import java.util.concurrent.TimeUnit.MILLISECONDS

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface DateProvider {
  fun getCurrentTime(): Long

  @Throws(ParseException::class)
  fun syncTime(
    date: String
  )
}

internal const val timeCorrectionKey = "timeCorrection"

class DateAdapter(
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