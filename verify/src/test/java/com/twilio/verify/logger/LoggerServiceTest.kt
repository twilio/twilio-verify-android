/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.logger

import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class LoggerServiceTest {

  @Test
  fun `Setting log level returns correct level`() {
    val logLevel = LogLevel.Info
    val loggerService: LoggerService = TestLoggerService(logLevel)
    assertEquals(logLevel, loggerService.logLevel)
  }

  @Test
  fun `Call log service method calls logger service method`() {
    val logLevel = LogLevel.Info
    val message = "Message"
    val loggerService = spy(TestLoggerService(logLevel))
    loggerService.log(logLevel, message)
    verify(loggerService).log(logLevel, message)
  }
}

class TestLoggerService(override val logLevel: LogLevel) : LoggerService {

  override fun log(
    logLevel: LogLevel,
    message: String,
    throwable: Throwable?
  ) {
  }
}
