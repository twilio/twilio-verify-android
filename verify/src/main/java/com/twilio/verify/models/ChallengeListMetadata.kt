package com.twilio.verify.models

internal class ChallengeListMetadata(
  override val page: Int,
  override val pageSize: Int,
  override val nextPageURL: String?,
  override val key: String
) : Metadata

