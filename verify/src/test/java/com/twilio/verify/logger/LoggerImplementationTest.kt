/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.logger

import com.google.common.util.concurrent.MoreExecutors
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.twilio.security.logger.Level
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LoggerImplementationTest {

  @Before
  fun setup() {
    LoggerImplementation.services.clear()
    LoggerImplementation.executorService = MoreExecutors.newDirectExecutorService()
  }

  @Test
  fun `Add logger service to logger`() {
    val logService: LoggerService = mock()
    LoggerImplementation.addService(logService)
    assertTrue(LoggerImplementation.services.contains(logService))
  }

  @Test
  fun `Log info level should call logger services for info`() {
    val message = "Message"
    val logService1: LoggerService = mock {
      on { logLevel }.doReturn(LogLevel.Info)
    }
    val logService2: LoggerService = mock {
      on { logLevel }.doReturn(LogLevel.Debug)
    }
    LoggerImplementation.addService(logService1)
    LoggerImplementation.addService(logService2)
    LoggerImplementation.log(Level.Info, message)
    verify(logService1).log(eq(LogLevel.Info), eq(message), anyOrNull())
    verify(logService2, never()).log(any(), any(), anyOrNull())
  }

  @Test
  fun `Log info level should call logger services for info and all`() {
    val message = "Message"
    val logService1: LoggerService = mock {
      on { logLevel }.doReturn(LogLevel.Info)
    }
    val logService2: LoggerService = mock {
      on { logLevel }.doReturn(LogLevel.All)
    }
    LoggerImplementation.addService(logService1)
    LoggerImplementation.addService(logService2)
    LoggerImplementation.log(Level.Info, message)
    verify(logService1).log(eq(LogLevel.Info), eq(message), anyOrNull())
    verify(logService2).log(eq(LogLevel.Info), eq(message), anyOrNull())
  }
}
