package com.twilio.verify.data

import android.content.SharedPreferences
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.NetworkError
import com.twilio.verify.networking.HttpMethod
import com.twilio.verify.networking.NetworkException
import java.net.URL
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.net.ssl.HttpsURLConnection

/*
 * Copyright (c) 2020, Twilio Inc.
 */

interface DateProvider {
  fun getCurrentTime(): Long

  fun syncTime(
    url: URL,
    success: () -> Unit
  )
}

private const val dateHeaderKey = "Date"
private const val timeCorrectionKey = "timeCorrection"

class DateAdapter(private val preferences: SharedPreferences) : DateProvider {
  override fun getCurrentTime(): Long {
    val timeDifference = preferences.getLong(
        timeCorrectionKey, 0)
    return localTime() + timeDifference
  }

  override fun syncTime(
    url: URL,
    success: () -> Unit
  ) {
    var httpUrlConnection: HttpsURLConnection? = null
    try {
      httpUrlConnection = url.openConnection() as HttpsURLConnection
      httpUrlConnection.requestMethod = HttpMethod.Head.method
      when (val responseCode = httpUrlConnection.responseCode) {
        200 -> {
          httpUrlConnection.headerFields[dateHeaderKey]?.first()
              ?.let {
                saveTime(fromRFC1123Date(it).time)
                success()
              } ?: run {
            throw TwilioVerifyException(
                NetworkException(Throwable("Invalid date value for syncing")), NetworkError
            )
          }
        }
        else ->
          throw TwilioVerifyException(
              NetworkException(
                  Throwable("Invalid response code $responseCode while syncing time")
              ), NetworkError
          )
      }
    } catch (e: Exception) {
      throw TwilioVerifyException(
          NetworkException(e), NetworkError
      )
    } finally {
      httpUrlConnection?.disconnect()
    }
  }

  private fun saveTime(time: Long) {
    val timeCorrection =
      MILLISECONDS.toSeconds(time) - localTime()
    preferences.edit()
        .putLong(timeCorrectionKey, timeCorrection)
        .apply()
  }

  private fun localTime() = MILLISECONDS.toSeconds(System.currentTimeMillis())
}