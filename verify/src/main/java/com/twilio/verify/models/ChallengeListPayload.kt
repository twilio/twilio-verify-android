package com.twilio.verify.models

/**
 * Describes the information required to fetch a **ChallengeList**
 */
class ChallengeListPayload(
  /**
   * The unique SID identifier of the Factor to which the **ChallengeList** is related
   */
  val factorSid: String,
  /**
   * Number of Challenges to be returned by the service
   */
  val pageSize: Int,
  /**
   * Status to filter the Challenges, if nothing is sent, Challenges of all status will be returned
   */
  val status: ChallengeStatus? = null,
  /**
   * Token used to retrieve the next page in the pagination arrangement
   */
  val pageToken: String? = null
)