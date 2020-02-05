/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.crypto

class KeyException constructor(
  message: String?,
  exception: Exception?
) : Exception(message, exception) {
  constructor(exception: Exception) : this(exception.message, exception)
}