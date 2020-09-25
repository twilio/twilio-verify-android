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

package com.twilio.verify.threading

import android.os.Handler
import android.os.Looper
import com.twilio.verify.TwilioVerifyException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias SafeSuccessResult<T> = (T) -> Unit
typealias SafeError<E> = (E) -> Unit
typealias SafeSuccess = () -> Unit

private val executorService: ExecutorService = Executors.newFixedThreadPool(10)

internal fun <T> execute(
  success: (T) -> Unit,
  error: (TwilioVerifyException) -> Unit,
  block: (onSuccess: SafeSuccessResult<T>, onError: SafeError<TwilioVerifyException>) -> Unit
) {
  executorService.execute(Task(block, success, error))
}

internal fun execute(
  success: () -> Unit,
  error: (TwilioVerifyException) -> Unit,
  block: (onSuccess: SafeSuccess, onError: SafeError<TwilioVerifyException>) -> Unit
) {
  executorService.execute(
    Task<Unit, TwilioVerifyException>(
      { onSuccess, onError ->
        block(
          { onSuccess(Unit) },
          onError
        )
      },
      { success() }, error
    )
  )
}

internal class Task<T, E : Exception>(
  private val block: (onSuccess: SafeSuccessResult<T>, onError: SafeError<E>) -> Unit,
  private val success: (T) -> Unit,
  private val error: (E) -> Unit,
  private val handler: Handler? = Looper.myLooper()
    ?.takeIf { it == Looper.getMainLooper() }
    ?.let {
      Handler(it)
    }
) : Runnable {
  override fun run() {
    block(::safeSuccess, ::safeError)
  }

  private fun safeSuccess(result: T) {
    handler?.let {
      handler.post { success(result) }
    } ?: run { success(result) }
  }

  private fun safeError(exception: E) {
    handler?.let {
      handler.post { error(exception) }
    } ?: run { error(exception) }
  }
}
