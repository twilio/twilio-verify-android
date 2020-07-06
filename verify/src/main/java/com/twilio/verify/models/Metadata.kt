package com.twilio.verify.models

interface Metadata {
  val page: Int
  val pageSize: Int
  val previousPageToken: String?
  val nextPageToken: String?
}