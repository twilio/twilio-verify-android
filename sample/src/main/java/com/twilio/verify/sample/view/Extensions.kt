/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.view

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.Factor
import com.twilio.verify.models.Service

fun Exception.showError(
  content: View
) {
  printStackTrace()
  Snackbar.make(content, message.toString(), BaseTransientBottomBar.LENGTH_LONG)
      .show()
}

fun ChallengeDetails.string(context: Context?): String {
  return takeIf { it.date != null || it.fields.isNotEmpty() }?.let {
    "Details:\n${fields.joinToString(
        "\n"
    ) {
      "  ${it.label} = ${it.value}"
    }}${(date?.let {
      "  Date = ${DateUtils.formatDateTime(context, it.time, FORMAT_SHOW_DATE or FORMAT_SHOW_TIME)}"
    } ?: "")}\n"
  } ?: ""
}

fun Challenge.string(context: Context?) =
  "Sid:\n${this.sid}\nName: ${this.challengeDetails.message}\nStatus: ${this.status}\nExpire on: ${DateUtils.formatDateTime(
      context, expirationDate.time, FORMAT_SHOW_DATE or FORMAT_SHOW_TIME
  )}"

fun Factor.string() = "Sid:\n${this.sid}\nName: ${this.friendlyName}\nStatus: ${this.status}"

fun Service.string() = "Sid:\n${this.sid}\nName: ${this.friendlyName}"