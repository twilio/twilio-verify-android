package com.twilio.verify.sample.model

import com.twilio.verify.models.FactorType

/*
 * Copyright (c) 2020, Twilio Inc.
 */

data class EnrollmentResponse(
  val token: String,
  val serviceSid: String,
  val identity: String,
  val factorType: String
)

fun EnrollmentResponse.getFactorType(): FactorType = FactorType.values()
    .associateBy(FactorType::factorTypeName)[factorType] ?: throw IllegalArgumentException(
    "Invalid response"
)