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

/**
 * Log levels
 *
 * @property level
 */
enum class LogLevel(internal val level: Level) {
  /**
   * Reports behaviors that shouldn't be happening
   *
   */
  Error(Level.Error),

  /**
   * Warns specific information of what is being done
   *
   */
  Info(Level.Info),

  /**
   * Specific data for the networking work, such as request body, headers, response code, response body
   *
   */
  Networking(Level.Networking),

  /**
   * Detailed information
   *
   */
  Debug(Level.Debug),

  /**
   * **Error**, **Info**, **Debug** and **Networking** are enabled
   *
   */
  All(Level.All)
}
