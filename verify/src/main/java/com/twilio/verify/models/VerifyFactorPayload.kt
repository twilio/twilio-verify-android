package com.twilio.verify.models

/*
 * Copyright (c) 2020, Twilio Inc.
 */

/**
 * Describes the information required to verify a **Factor**
 */
interface VerifyFactorPayload {
  /**
   * Factor sid
   */
  val sid: String
}