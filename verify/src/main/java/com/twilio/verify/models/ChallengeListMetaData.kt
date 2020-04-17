package com.twilio.verify.models

class ChallengeListMetaData(
  override val page: Int,
  override val pageSize: Int,
  override val nextPageURL: String?,
  override val key: String
) : MetaData

