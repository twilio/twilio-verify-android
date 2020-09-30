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

import android.util.Log
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class LogWrapperTest {

  @Test
  fun `Log error with tag and message`() {
    val tag = "tag"
    val message = "twilio"
    LogWrapper.error(tag, message)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(message, log.msg)
    assertEquals(Log.ERROR, log.type)
    assertNull(log.throwable)
  }

  @Test
  fun `Log error with tag, message and throwable`() {
    val tag = "tag"
    val message = "twilio"
    val throwable = IOException()
    LogWrapper.error(tag, message, throwable)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(message, log.msg)
    assertEquals(throwable, log.throwable)
    assertEquals(Log.ERROR, log.type)
  }

  @Test
  fun `Log info with tag and message`() {
    val tag = "tag"
    val message = "twilio"
    LogWrapper.info(tag, message)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(message, log.msg)
    assertEquals(Log.INFO, log.type)
  }

  @Test
  fun `Log info with tag, message and throwable`() {
    val tag = "tag"
    val message = "twilio"
    val throwable = IOException()
    LogWrapper.info(tag, message, throwable)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(message, log.msg)
    assertEquals(throwable, log.throwable)
    assertEquals(Log.INFO, log.type)
  }

  @Test
  fun `Log debug with tag and message`() {
    val tag = "tag"
    val message = "twilio"
    LogWrapper.debug(tag, message)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(message, log.msg)
    assertEquals(Log.DEBUG, log.type)
  }

  @Test
  fun `Log debug with tag, message and throwable`() {
    val tag = "tag"
    val message = "twilio"
    val throwable = IOException()
    LogWrapper.debug(tag, message, throwable)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(throwable, log.throwable)
    assertEquals(Log.DEBUG, log.type)
  }

  @Test
  fun `Log verbose with tag and message`() {
    val tag = "tag"
    val message = "twilio"
    LogWrapper.verbose(tag, message)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(message, log.msg)
    assertEquals(Log.VERBOSE, log.type)
  }

  @Test
  fun `Log verbose with tag, message and throwable`() {
    val tag = "tag"
    val message = "twilio"
    val throwable = IOException()
    LogWrapper.verbose(tag, message, throwable)
    val log = ShadowLog.getLogs().last()
    assertEquals(tag, log.tag)
    assertEquals(throwable, log.throwable)
    assertEquals(Log.VERBOSE, log.type)
  }
}
