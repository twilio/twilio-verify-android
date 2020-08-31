/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

/**
 * Describes the information required to update a **Challenge**
 */
interface UpdateChallengePayload {
  /**
   * Sid of the Factor to which the Challenge is related
   */
  val factorSid: String
  /**
   * Sid of the Challenge to be updated
   */
  val challengeSid: String
}
