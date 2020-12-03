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

/**
 * Logging service that allows to create custom services
 *
 */
interface LoggerService {
  /**
   * Set a log level to log processes happening into the SDK
   */
  val logLevel: LogLevel

  /**
   * Will be triggered to log information
   *
   * @param logLevel
   * @param message
   * @param throwable
   */
  fun log(logLevel: LogLevel, message: String, throwable: Throwable? = null)
}
