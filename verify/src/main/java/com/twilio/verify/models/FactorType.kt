/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

/**
 * Describes the types a factor can have
 */
enum class FactorType(val factorTypeName: String) {
  /**
   * Push type
   */
  PUSH("push")
}
