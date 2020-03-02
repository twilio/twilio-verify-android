/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

interface UpdateChallengeInput {
  val factorSid: String
  val challengeSid: String
}