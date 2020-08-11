package com.twilio.verify.models

/*
 * Copyright (c) 2020, Twilio Inc.
 */

/**
 * Describes the information required to verify a **Factor** which type is **Push**
 */
class VerifyPushFactorPayload(
  /**
   * Factor sid
   */
  override val sid: String
) : VerifyFactorPayload