/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.sample

import com.twilio.verify.models.Challenge
import com.twilio.verify.models.Factor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object VerifyEventBus {
  val bus: ConflatedBroadcastChannel<VerifyEvent> = ConflatedBroadcastChannel()

  fun send(
    event: VerifyEvent,
    context: CoroutineDispatcher = Dispatchers.Main
  ) {
    CoroutineScope(context).launch {
      if (!bus.isClosedForSend) {
        bus.offer(event)
      }
    }
  }

  suspend inline fun <reified T : VerifyEvent> consumeEvent(crossinline consume: (T) -> Unit) =
    bus.asFlow().filter { it is T }.map { it as T }.collect { consume(it) }
}

sealed class VerifyEvent
class VerifiedFactor(val factor: Factor) : VerifyEvent()
class NewChallenge(val challenge: Challenge) : VerifyEvent()
