/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

/**
 * Describes the information required to update a **Push Challenge**
 */
class UpdatePushChallengePayload(
  /**
   * Sid of the Factor to which the Challenge is related
   */
  override val factorSid: String,
  /**
   * Sid of the Challenge to be updated
   */
  override val challengeSid: String,
  /**
   * New status of the challenge
   */
  val status: ChallengeStatus
) : UpdateChallengePayload
