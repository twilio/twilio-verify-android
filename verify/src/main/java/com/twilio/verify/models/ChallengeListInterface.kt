package com.twilio.verify.models

interface ChallengeListInterface {
  val challenges: ArrayList<Challenge>
  val metadata: Metadata
}