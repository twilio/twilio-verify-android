/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

import java.util.Date

/**
 * Describes the information of a **Factor**
 */
interface Factor {
  /**
   * Status of the Factor
   */
  var status: FactorStatus
  /**
   * Id of the Factor
   */
  val sid: String
  /**
   * Friendly name of the factor, can be used for display purposes
   */
  val friendlyName: String
  /**
   * Id of the account to which the Factor is related
   */
  val accountSid: String
  /**
   * Id of the service to which the Factor is related
   */
  val serviceSid: String
  /**
   * Identifies the user, should be an UUID you should not use PII (Personal Identifiable Information)
   * because the systems that will process this attribute assume it is not directly identifying information.
   */
  val identity: String
  /**
   * Type of the Factor
   */
  val type: FactorType
  /**
   * Indicates the creation date of the Factor
   */
  val createdAt: Date
}

/**
 * Describes the verification status of a factor
 */
enum class FactorStatus(val value: String) {
  /**
   * The factor is verified and is ready to recevie challenges
   */
  Verified("verified"),
  /**
   * The factor is not yet verified and can't receive challenges
   */
  Unverified("unverified")
}