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

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal const val dateFormatTimeZone = "yyyy-MM-dd'T'HH:mm:ssZ"
internal val dateFormatterTimeZone = SimpleDateFormat(dateFormatTimeZone, Locale.US)
private const val dateFormatUTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private const val RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
private val dateFormatterUTC =
  SimpleDateFormat(dateFormatUTC, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

internal fun fromRFC3339Date(date: String): Date {
  try {
    if (date.endsWith("Z")) {
      return dateFormatterUTC.parse(date)
    }
    val firstPart: String = date.substring(0, date.lastIndexOf('-'))
    var secondPart: String = date.substring(date.lastIndexOf('-'))

    secondPart = (
      secondPart.substring(0, secondPart.indexOf(':')) +
        secondPart.substring(secondPart.indexOf(':') + 1)
      )
    val dateString = firstPart + secondPart
    return dateFormatterTimeZone.parse(dateString)
  } catch (e: ParseException) {
    throw e
  } catch (e: Exception) {
    throw ParseException(e.message, 0)
  }
}

internal fun toRFC3339Date(date: Date): String {
  return dateFormatterUTC.format(date)
}

internal fun fromRFC1123Date(date: String): Date {
  try {
    return SimpleDateFormat(RFC1123_FORMAT, Locale.US).parse(date)
  } catch (e: ParseException) {
    throw e
  } catch (e: Exception) {
    throw ParseException(e.message, 0)
  }
}
