/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.threading

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val executorService: ExecutorService = Executors.newFixedThreadPool(10)

fun <T, TwilioVerifyException> execute(
  success: (T) -> Unit,
  error: (TwilioVerifyException) -> Unit,
  block: (success: (T) -> Unit, error: (TwilioVerifyException) -> Unit) -> Unit
) {
  executorService.execute(Task(block, success, error))
}

class Task<T, TwilioVerifyException>(
  private val block: (success: (T) -> Unit, error: (TwilioVerifyException) -> Unit) -> Unit,
  private val onSuccess: (T) -> Unit,
  private val onError: (TwilioVerifyException) -> Unit,
  private val handler: Handler? = Looper.myLooper()?.takeIf { it == Looper.getMainLooper() }?.let {
    Handler(it)
  }
) : Runnable {
  override fun run() {
    block(
        { result -> success(result) },
        { exception -> error(exception) }
    )
  }

  private fun success(result: T) {
    handler?.let {
      handler.post { onSuccess(result) }
    } ?: run { onSuccess(result) }
  }

  private fun error(exception: TwilioVerifyException) {
    handler?.let {
      handler.post { onError(exception) }
    } ?: run { onError(exception) }
  }
}