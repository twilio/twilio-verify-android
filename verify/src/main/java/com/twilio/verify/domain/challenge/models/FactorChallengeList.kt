package com.twilio.verify.domain.challenge.models

import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.Metadata

/**
 * Describes the information of a **ChallengeList**
 */
internal class FactorChallengeList(
  /**
   * List of Challenges that matches the parameters of the **ChallengeListPayload** used
   */
  override val challenges: List<Challenge>,
  /**
   * Metadata returned by the /Challenges endpoint, used to fetch subsequent pages of Challenges
   */
  override val metadata: Metadata
) : ChallengeList
