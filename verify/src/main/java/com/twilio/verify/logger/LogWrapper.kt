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

interface LogAdapter {
  fun error(tag: String, message: String?, throwable: Throwable?)
  fun info(tag: String, message: String?, throwable: Throwable?)
  fun debug(tag: String, message: String?, throwable: Throwable?)
  fun verbose(tag: String, message: String?, throwable: Throwable?)
}

object LogWrapper : LogAdapter {
  override fun error(tag: String, message: String?, throwable: Throwable?) {
    Log.e(tag, message, throwable)
  }

  override fun info(tag: String, message: String?, throwable: Throwable?) {
    Log.i(tag, message, throwable)
  }

  override fun debug(tag: String, message: String?, throwable: Throwable?) {
    Log.d(tag, message, throwable)
  }

  override fun verbose(tag: String, message: String?, throwable: Throwable?) {
    Log.v(tag, message, throwable)
  }
}
