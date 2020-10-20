/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.logger

import com.google.common.util.concurrent.MoreExecutors
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LoggerTest {

  @Before
  fun setup() {
    Logger.services.clear()
    Logger.executorService = MoreExecutors.newDirectExecutorService()
  }

  @Test
  fun `Add logger service to logger`() {
    val logService: LogService = mock()
    Logger.addService(logService)
    assertTrue(Logger.services.contains(logService))
  }

  @Test
  fun `Log info level should call logger services for info`() {
    val message = "Message"
    val logService1: LogService = mock {
      on { level }.doReturn(Level.Info)
    }
    val logService2: LogService = mock {
      on { level }.doReturn(Level.Debug)
    }
    Logger.addService(logService1)
    Logger.addService(logService2)
    Logger.log(Level.Info, message)
    verify(logService1).log(eq(Level.Info), eq(message), anyOrNull())
    verify(logService2, never()).log(any(), any(), anyOrNull())
  }

  @Test
  fun `Log info level should call logger services for info and all`() {
    val message = "Message"
    val logService1: LogService = mock {
      on { level }.doReturn(Level.Info)
    }
    val logService2: LogService = mock {
      on { level }.doReturn(Level.All)
    }
    Logger.addService(logService1)
    Logger.addService(logService2)
    Logger.log(Level.Info, message)
    verify(logService1).log(eq(Level.Info), eq(message), anyOrNull())
    verify(logService2).log(eq(Level.Info), eq(message), anyOrNull())
  }
}
