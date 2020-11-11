/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.logger

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultLoggerServiceTest {

  private val log: LogWrapper = mock()

  @Test
  fun `LogLevel set to ALL should log INFO types`() {
    val loggerService = DefaultLoggerService(LogLevel.All, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Info, message)
    verify(log).info(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).error(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ALL should log ERROR types`() {
    val loggerService = DefaultLoggerService(LogLevel.All, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Error, message)
    verify(log).error(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ALL should log NETWORKING types`() {
    val loggerService = DefaultLoggerService(LogLevel.All, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Networking, message)
    verify(log).verbose(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).error(any(), any(), any())
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ALL should log DEBUG types`() {
    val loggerService = DefaultLoggerService(LogLevel.All, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Debug, message)
    verify(log).debug(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).error(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).info(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ERROR should log ERROR types`() {
    val loggerService = DefaultLoggerService(LogLevel.Error, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Error, message)
    verify(log).error(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ERROR shouldn't log INFO types`() {
    val loggerService = DefaultLoggerService(LogLevel.Error, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Info, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ERROR shouldn't log NETWORKING types`() {
    val loggerService = DefaultLoggerService(LogLevel.Error, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Networking, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to ERROR shouldn't log DEBUG types`() {
    val loggerService = DefaultLoggerService(LogLevel.Error, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Debug, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to INFO should log INFO types`() {
    val loggerService = DefaultLoggerService(LogLevel.Info, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Info, message)
    verify(log).info(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).error(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
  }

  @Test
  fun `LogLevel set to INFO shouldn't log ERROR types`() {
    val loggerService = DefaultLoggerService(LogLevel.Info, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Error, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to INFO shouldn't log NETWORKING types`() {
    val loggerService = DefaultLoggerService(LogLevel.Info, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Networking, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to INFO shouldn't log DEBUG types`() {
    val loggerService = DefaultLoggerService(LogLevel.Info, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Debug, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to NETWORKING should log NETWORKING types`() {
    val loggerService = DefaultLoggerService(LogLevel.Networking, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Networking, message)
    verify(log).verbose(
      check { tag ->
        assertEquals(DefaultLoggerService.tag, tag)
      },
      eq(message), eq(null)
    )
    verify(log, never()).error(any(), any(), any())
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
  }

  @Test
  fun `LogLevel set to NETWORKING shouldn't log ERROR types`() {
    val loggerService = DefaultLoggerService(LogLevel.Networking, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Error, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to NETWORKING shouldn't log INFO types`() {
    val loggerService = DefaultLoggerService(LogLevel.Networking, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Info, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }

  @Test
  fun `LogLevel set to NETWORKING shouldn't log DEBUG types`() {
    val loggerService = DefaultLoggerService(LogLevel.Networking, log)
    val message = "Twilio"
    loggerService.log(LogLevel.Debug, message)
    verify(log, never()).info(any(), any(), any())
    verify(log, never()).verbose(any(), any(), any())
    verify(log, never()).debug(any(), any(), any())
    verify(log, never()).error(any(), any(), any())
  }
}
