package com.twilio.verify.domain.challenge.models

import com.twilio.verify.models.Metadata

internal class ChallengeListMetadata(
  override val page: Int,
  override val pageSize: Int,
  override val previousPageToken: String?,
  override val nextPageToken: String?
) : Metadata
