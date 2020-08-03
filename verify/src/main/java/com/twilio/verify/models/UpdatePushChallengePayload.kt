/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

/**
 * Describes the information required to update a **Push Challenge**
 */
class UpdatePushChallengePayload(
  /**
   * Id of the Factor to which the Challenge is related
   */
  override val factorSid: String,
  /**
   * Id of the Challenge to be updated
   */
  override val challengeSid: String,
  /**
   * Id of the Challenge to be updated
   */
  val status: ChallengeStatus
) : UpdateChallengePayload