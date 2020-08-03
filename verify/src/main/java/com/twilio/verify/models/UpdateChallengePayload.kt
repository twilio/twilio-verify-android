/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

/**
 * Describes the information required to update a **Challenge**
 */
interface UpdateChallengePayload {
  /**
   * Id of the Factor to which the Challenge is related
   */
  val factorSid: String
  /**
   * Id of the Challenge to be updated
   */
  val challengeSid: String
}