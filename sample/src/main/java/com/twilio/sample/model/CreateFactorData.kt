package com.twilio.sample.model

data class CreateFactorData(
  val jwtUrl: String,
  val identity: String,
  val factorName: String,
  val pushToken: String
)