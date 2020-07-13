package com.twilio.verify.models

internal class ChallengeListMetadata(
  override val page: Int,
  override val pageSize: Int,
  override val previousPageToken: String?,
  override val nextPageToken: String?
) : Metadata

