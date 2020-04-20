package com.twilio.verify.models

interface ChallengeListContract {
  val challenges: ArrayList<Challenge>
  val metadata: Metadata
}