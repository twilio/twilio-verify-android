/*
 * Copyright (c) 2020, Twilio Inc.
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

fun <T> execute(
  success: (T) -> Unit,
  error: (TwilioVerifyException) -> Unit,
  block: (onSuccess: SafeSuccessResult<T>, onError: SafeError<TwilioVerifyException>) -> Unit
) {
  executorService.execute(Task(block, success, error))
}

fun execute(
  success: () -> Unit,
  error: (TwilioVerifyException) -> Unit,
  block: (onSuccess: SafeSuccess, onError: SafeError<TwilioVerifyException>) -> Unit
) {
  executorService.execute(
      Task<Unit, TwilioVerifyException>({ onSuccess, onError ->
        block(
            { onSuccess(Unit) },
            onError
        )
      }, { success() }, error)
  )
}

class Task<T, E : Exception>(
  private val block: (onSuccess: SafeSuccessResult<T>, onError: SafeError<E>) -> Unit,
  private val success: (T) -> Unit,
  private val error: (E) -> Unit,
  private val handler: Handler? = Looper.myLooper()?.takeIf { it == Looper.getMainLooper() }?.let {
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