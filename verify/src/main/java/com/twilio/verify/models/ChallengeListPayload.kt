package com.twilio.verify.models

/**
 * escribes the information required to fetch a **ChallengeList**
 */
class ChallengeListPayload(
  /**
   * Id of the factor to which the Challenge is related
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
   *
   */
  val pageToken: String? = null
)