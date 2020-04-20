package com.twilio.verify.models

interface Metadata {
  val page: Int
  val pageSize: Int
  val nextPageURL: String?
  val key: String
}