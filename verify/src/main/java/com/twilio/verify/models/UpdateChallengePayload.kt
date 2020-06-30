/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface UpdateChallengePayload {
  val factorSid: String
  val challengeSid: String
}