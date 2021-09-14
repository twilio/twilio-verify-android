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

package com.twilio.verify.sample.push

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object VerifyEventBus {

  val events = MutableSharedFlow<VerifyEvent>()

  fun send(
    event: VerifyEvent
  ) {
    send(event, Dispatchers.Main)
  }

  suspend inline fun <reified T : VerifyEvent> consumeEvent(
    crossinline consume: (T) -> Unit
  ) = events.asSharedFlow().filter { event -> event is T }.map { it as T }
    .collectLatest { consume(it) }

  private suspend fun invokeEvent(event: VerifyEvent) = events.emit(event)

  private fun send(
    event: VerifyEvent,
    context: CoroutineDispatcher = Dispatchers.Main
  ) {
    CoroutineScope(context).launch {
      invokeEvent(event)
    }
  }
}

sealed class VerifyEvent
class NewChallenge(
  val challengeSid: String,
  val factorSid: String
) : VerifyEvent()
