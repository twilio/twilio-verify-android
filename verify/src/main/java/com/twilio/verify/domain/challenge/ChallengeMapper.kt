/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Expired
import com.twilio.verify.models.Detail
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

internal const val sidKey = "sid"
internal const val messageKey = "message"
internal const val detailsKey = "details"
internal const val fieldsKey = "fields"
internal const val dateKey = "date"
internal const val labelKey = "label"
internal const val valueKey = "value"
internal const val hiddenDetailsKey = "hidden_details"
internal const val factorSidKey = "factor_sid"
internal const val statusKey = "status"
internal const val entitySidKey = "entity_sid"
internal const val createdDateKey = "date_created"
internal const val updatedDateKey = "date_updated"
internal const val expirationDateKey = "expiration_date"
internal const val dateFormatTimeZone = "yyyy-MM-dd'T'HH:mm:ssZ"
internal val dateFormatterTimeZone = SimpleDateFormat(dateFormatTimeZone)
private const val dateFormatUTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private val dateFormatterUTC =
  SimpleDateFormat(dateFormatUTC).apply { timeZone = TimeZone.getTimeZone("UTC") }

internal class ChallengeMapper {
  @Throws(TwilioVerifyException::class)
  fun fromApi(jsonObject: JSONObject): Challenge {
    try {
      val details = jsonObject.getString(detailsKey)
      val createdDate = jsonObject.getString(createdDateKey)
      val updatedDate = jsonObject.getString(updatedDateKey)
      return FactorChallenge(
          sid = jsonObject.getString(sidKey), details = details, createdDate = createdDate,
          updatedDate = updatedDate, factorSid = jsonObject.getString(factorSidKey),
          expirationDate = fromRFC3339Date(jsonObject.getString(expirationDateKey)),
          createdAt = fromRFC3339Date(createdDate),
          updatedAt = fromRFC3339Date(updatedDate),
          challengeDetails = toChallengeDetails(details),
          hiddenDetails = jsonObject.getString(hiddenDetailsKey),
          status = ChallengeStatus.values()
              .find { it.value == jsonObject.getString(statusKey) }
              ?: Expired, entitySid = jsonObject.getString(entitySidKey)
      )
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }

  private fun toChallengeDetails(details: String): ChallengeDetails = run {
    val detailsJson = JSONObject(details)
    val message = detailsJson.getString(messageKey)
    val fields = detailsJson.optJSONArray(fieldsKey)
        ?.takeIf { it.length() > 0 }
        ?.let {
          val fields = mutableListOf<Detail>()
          for (i in 0 until it.length()) {
            val jsonObject = it.getJSONObject(i)
            fields.add(
                Detail(
                    jsonObject.getString(labelKey),
                    jsonObject.getString(valueKey)
                )
            )
          }
          fields
        } ?: listOf<Detail>()
    val date = detailsJson.optString(dateKey)
        ?.takeIf { it.isNotEmpty() }
        ?.let { fromRFC3339Date(it) }
    return ChallengeDetails(message, fields, date)
  }
}

internal fun fromRFC3339Date(date: String): Date {
  try {
    if (date.endsWith("Z")) {
      return dateFormatterUTC.parse(date)
    }
    val firstPart: String = date.substring(0, date.lastIndexOf('-'))
    var secondPart: String = date.substring(date.lastIndexOf('-'))

    secondPart = (secondPart.substring(0, secondPart.indexOf(':'))
        + secondPart.substring(secondPart.indexOf(':') + 1))
    val dateString = firstPart + secondPart
    return dateFormatterTimeZone.parse(dateString)
  } catch (e: ParseException) {
    throw e
  } catch (e: Exception) {
    throw ParseException(e.message, 0)
  }
}