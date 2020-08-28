/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto.key.template

internal interface Template {
  val alias: String
  val algorithm: String
  val shouldExist: Boolean
}
