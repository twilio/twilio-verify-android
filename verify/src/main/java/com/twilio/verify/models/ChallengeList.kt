package com.twilio.verify.models

/**
 * Describes the information of a **ChallengeList**
 */
interface ChallengeList {
  /**
   * List of Challenges that matches the parameters of the **ChallengeListPayload** used
   */
  val challenges: List<Challenge>
  /**
   * Metadata returned by the /Challenges endpoint, used to fetch subsequent pages of Challenges
   */
  val metadata: Metadata
}
