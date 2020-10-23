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

import com.twilio.verify.BuildConfig

internal class DefaultLoggerService(override val logLevel: LogLevel, private val log: LogAdapter = LogWrapper) : LoggerService {
  override fun log(logLevel: LogLevel, message: String, throwable: Throwable?) {
    if ((this.logLevel != LogLevel.All && this.logLevel != logLevel)) {
      return
    }
    val trace = Exception().stackTrace[2]
    val tag = getTraceTag(trace)
    when (logLevel) {
      LogLevel.Error -> log.error(tag, message, throwable)
      LogLevel.Info -> log.info(tag, message, throwable)
      LogLevel.Debug -> log.debug(tag, message, throwable)
      else -> log.verbose(tag, message, throwable)
    }
  }

  private fun getTraceTag(trace: StackTraceElement): String {
    val className = trace.className.split(".").last()
    return "${BuildConfig.LIBRARY_PACKAGE_NAME}.$className.${trace.methodName}"
  }
}
