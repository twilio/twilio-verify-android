package com.twilio.verify.models

/*
 * Copyright (c) 2020, Twilio Inc.
 */

/**
 * Describes the information required to update a **Factor**
 */
interface UpdateFactorPayload {
  /**
   * Factor sid
   */
  val sid: String
}