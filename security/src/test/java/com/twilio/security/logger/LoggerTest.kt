/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.security.logger

import com.nhaarman.mockitokotlin2.mock
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
  }

  @Test
  fun `Add logger service to logger`() {
    val loggerService: LoggerService = mock()
    Logger.addService(loggerService)
    assertTrue(Logger.services.contains(loggerService))
  }
}
