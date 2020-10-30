/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.logger

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class LoggerTest {

  @Test
  fun `Call logger contract when logging`() {
    val level = Level.Debug
    val message = "Message"
    val throwable = IllegalArgumentException("test")
    val loggerContract: LoggerContract = mock()
    Logger.loggerContract = loggerContract
    Logger.log(level, message, throwable)
    verify(loggerContract).log(level, message, throwable)
  }
}
