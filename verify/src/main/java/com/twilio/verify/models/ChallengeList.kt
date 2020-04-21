package com.twilio.verify.models

interface ChallengeList {
  val challenges: List<Challenge>
  val metadata: Metadata
}