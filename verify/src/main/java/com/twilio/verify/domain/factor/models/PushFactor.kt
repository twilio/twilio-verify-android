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

package com.twilio.verify.domain.factor.models

import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorType
import com.twilio.verify.models.FactorType.PUSH
import java.util.Date

internal class PushFactor(
  override val sid: String,
  override val friendlyName: String,
  override val accountSid: String,
  override val serviceSid: String,
  override val identity: String,
  override var status: FactorStatus = Unverified,
  override val createdAt: Date,
  val config: Config
) : Factor {
  override val type: FactorType = PUSH

  var keyPairAlias: String? = null
}

internal data class Config(
  internal val credentialSid: String,
  internal val notificationPlatform: NotificationPlatform = NotificationPlatform.FCM
)

internal enum class NotificationPlatform(val value: String) {
  FCM("fcm"),
  None("none")
}
