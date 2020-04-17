package com.twilio.verify.models

interface MetaData {
  val page: Int
  val pageSize: Int
  val nextPageURL: String?
  val key: String
}