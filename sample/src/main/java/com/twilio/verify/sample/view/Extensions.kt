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

package com.twilio.verify.sample.view

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.twilio.verify.InputException
import com.twilio.verify.models.ChallengeDetails

fun Throwable.showError(
  content: View
) {
  printStackTrace()
  var message = message.toString()
  (cause as? InputException)?.let {
    message = "$message. ${it.message}"
  }
  Snackbar.make(content, message, BaseTransientBottomBar.LENGTH_LONG).show()
}

fun ChallengeDetails.string(context: Context?): String {
  return takeIf { it.date != null || it.fields.isNotEmpty() }?.let {
    "Details:\n${
    fields.joinToString(
      "\n"
    ) {
      "  ${it.label} = ${it.value}"
    }
    }${
    (
      date?.let {
        "\n  Date = ${
        DateUtils.formatDateTime(
          context,
          it.time,
          FORMAT_SHOW_DATE or FORMAT_SHOW_TIME
        )
        }"
      } ?: ""
      )
    }\n"
  } ?: ""
}
