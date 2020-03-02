/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch

object EventBus {
  val bus: BroadcastChannel<Any> = ConflatedBroadcastChannel()

  fun send(
    event: Any,
    context: CoroutineDispatcher = Dispatchers.Main
  ) {
    CoroutineScope(context).launch {
      bus.send(event)
    }
  }

  inline fun <reified T> asChannel(): ReceiveChannel<T> {
    return bus.openSubscription()
        .filter { it is T }
        .map { it as T }
  }
}

data class VerifiedFactor(val factorSid: String)
