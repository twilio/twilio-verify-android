/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

class UpdatePushChallengeInput(
  override val factorSid: String,
  override val challengeSid: String,
  override val status: ChallengeStatus
) : UpdateChallengeInput