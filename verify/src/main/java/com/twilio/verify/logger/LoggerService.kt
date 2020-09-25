/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.logger

interface LoggerService {
  fun log(level: LogLevel, message: String)
}
