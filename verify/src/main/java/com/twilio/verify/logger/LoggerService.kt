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

import com.twilio.security.logger.Level
import com.twilio.security.logger.LogService

interface LoggerService : LogService {
  val logLevel: LogLevel
  override val level: Level
    get() = logLevel.level

  fun log(logLevel: LogLevel, message: String, throwable: Throwable? = null)

  override fun log(level: Level, message: String, throwable: Throwable?) {
    LogLevel.values().firstOrNull { it.level == level }?.let { log(it, message, throwable) }
  }
}
