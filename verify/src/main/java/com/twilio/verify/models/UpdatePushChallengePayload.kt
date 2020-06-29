/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

class UpdatePushChallengePayload(
  override val factorSid: String,
  override val challengeSid: String,
  val status: ChallengeStatus
) : UpdateChallengePayload