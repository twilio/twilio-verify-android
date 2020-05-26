/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.data

import android.util.Base64

fun encodeToUTF8String(
  input: ByteArray,
  flags: Int
): String {
  val encoded = Base64.encode(input, flags)
  return String(encoded)
}