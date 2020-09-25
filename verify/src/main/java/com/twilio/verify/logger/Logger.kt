package com.twilio.verify.logger

/*
 * Copyright (c) 2020, Twilio Inc.
 */
internal object Logger {
  internal val services: MutableList<LoggerService> = mutableListOf()

  fun addService(loggerService: LoggerService) {
    services.add(loggerService)
  }
}
