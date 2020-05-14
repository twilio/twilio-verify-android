package com.twilio.verify.sample.model

data class CreateFactorData(
  val identity: String,
  val factorName: String,
  val pushToken: String
)