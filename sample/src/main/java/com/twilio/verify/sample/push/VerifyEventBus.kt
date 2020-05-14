/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.push

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object VerifyEventBus {
  val bus: BroadcastChannel<VerifyEvent> = BroadcastChannel(BUFFERED)

  fun send(
    event: VerifyEvent
  ) {
    send(event, Dispatchers.Main)
  }

  private fun send(
    event: VerifyEvent,
    context: CoroutineDispatcher = Dispatchers.Main
  ) {
    CoroutineScope(context).launch {
      if (!bus.isClosedForSend) {
        bus.offer(event)
      }
    }
  }

  inline fun <reified T : VerifyEvent> consumeEvent(
    context: CoroutineDispatcher = Dispatchers.Main,
    crossinline consume: (T) -> Unit
  ) =
    CoroutineScope(context).launch {
      bus.asFlow()
          .filter { it is T }
          .map { it as T }
          .collect { consume(it) }
    }
}

sealed class VerifyEvent
class NewChallenge(
  val challengeSid: String,
  val factorSid: String
) : VerifyEvent()
