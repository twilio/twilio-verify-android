/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.logger

import com.twilio.security.logger.Level
import com.twilio.security.logger.LoggerContract
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object LoggerImplementation : LoggerContract {

  val services: MutableList<LoggerService> = mutableListOf()
  var executorService: ExecutorService = Executors.newFixedThreadPool(3)

  fun addService(logService: LoggerService) {
    services.add(logService)
  }

  override fun log(level: Level, message: String, throwable: Throwable?) {
    executorService.execute {
      LogLevel.values().firstOrNull { it.level == level }?.let { logLevel ->
        services.filter { it.logLevel == LogLevel.All || it.logLevel == logLevel }.forEach { service ->
          service.log(logLevel, message, throwable)
        }
      }
    }
  }
}
