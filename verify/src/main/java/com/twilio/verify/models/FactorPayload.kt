/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

/**
 * Describes the information required to create a Factor
 *
 * @property friendlyName: A human readable description of this resource, up to 64 characters. For a push factor, this can be the device's name.
 * @property serviceSid: The unique SID identifier of the Service.
 * @property identity: Identifies the user, should be an UUID you should not use PII (Personal Identifiable Information)
 * because the systems that will process this attribute assume it is not directly identifying information.
 * @property factorType: Type of the factor. @see <FactorType>.
 */

interface FactorPayload {
  val friendlyName: String
  val serviceSid: String
  val identity: String
  val factorType: FactorType
}