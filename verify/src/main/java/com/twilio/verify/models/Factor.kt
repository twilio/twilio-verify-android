/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.models

import java.util.Date

/**
 * Describes the information of a **Factor**.
 */
interface Factor {
  /**
   * Status of the Factor.
   */
  var status: FactorStatus
  /**
   * The unique SID identifier of the Factor.
   */
  val sid: String
  /**
   * A human readable description of this resource, up to 64 characters. For a push factor, this can be the device's name.
   */
  val friendlyName: String
  /**
   * The unique SID of the Account that created the Service resource.
   */
  val accountSid: String
  /**
   * The unique SID identifier of the Service to which the Factor is related.
   */
  val serviceSid: String
  /**
   * Identifies the user, should be an UUID you should not use PII (Personal Identifiable Information)
   * because the systems that will process this attribute assume it is not directly identifying information.
   */
  val identity: String
  /**
   * Type of the Factor. Currently only `push` is supported
   */
  val type: FactorType
  /**
   * Indicates the creation date of the Factor.
   */
  val createdAt: Date
}

/**
 * Describes the verification status of a factor.
 */
enum class FactorStatus(val value: String) {
  /**
   * The factor is verified and is ready to recevie challenges.
   */
  Verified("verified"),
  /**
   * The factor is not yet verified and can't receive challenges.
   */
  Unverified("unverified")
}
