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

enum class LogLevel(internal val level: Level? = null) {
  OFF(Level.OFF),
  ERROR(Level.ERROR),
  INFO(Level.INFO),
  NETWORKING(Level.NETWORKING),
  DEBUG(Level.DEBUG),
  ALL
}
