/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.models

import java.util.Date

/**
 * Describes the information of a **Challenge**
 */
interface Challenge {
  /**
   * The unique SID identifier of the Challenge
   */
  val sid: String
  /**
   * Details of the Challenge
   */
  val challengeDetails: ChallengeDetails
  /**
   * Hidden details of the Challenge
   */
  val hiddenDetails: String
  /**
   * Sid of the factor to which the Challenge is related
   */
  val factorSid: String
  /**
   * Status of the Challenge
   */
  val status: ChallengeStatus
  /**
   * Indicates the creation date of the Challenge
   */
  val createdAt: Date
  /**
   * Indicates the last date the Challenge was updated
   */
  val updatedAt: Date
  /**
   * Indicates the date in which the Challenge expires
   */
  val expirationDate: Date
}

/**
 * Describes the approval status of a **Challenge**
 */
enum class ChallengeStatus(val value: String) {
  /**
   * The Challenge is waiting to be approved or denied by the user
   */
  Pending("pending"),
  /**
   * The Challenge was approved by the user
   */
  Approved("approved"),
  /**
   * The Challenge was denied by the user
   */
  Denied("denied"),
  /**
   * The Challenge expired and can't no longer be approved or denied by the user
   */
  Expired("expired")
}

/**
 * Describes the Details of a **Challenge**
 */
data class ChallengeDetails(
  /**
   * Associated message of the Challenge
   */
  val message: String,
  /**
   * List with the additional details of a Challenge
   */
  val fields: List<Detail>,
  /**
   * Date attached by the customer only received if the service has `includeDate` turned on
   */
  val date: Date?
)

/**
 * Describes the information of a **Challenge Detail**
 */
data class Detail(
  /**
   * Detail's title
   */
  val label: String,
  /**
   * Detail's description
   */
  val value: String
)