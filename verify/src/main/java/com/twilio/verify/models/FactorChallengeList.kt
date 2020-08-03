package com.twilio.verify.models

/**
 * Describes the information of a **ChallengeList**
 */
class FactorChallengeList(
  /**
   * List of Challenges that matches the parameters of the **ChallengeListPayload** used
   */
  override val challenges: List<Challenge>,
  /**
   * Metadata returned by the /Challenges endpoint, used to fetch subsequent pages of Challenges
   */
  override val metadata: Metadata
) : ChallengeList