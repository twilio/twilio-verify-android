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
internal const val createdDateKey = "date_created"
internal const val updatedDateKey = "date_updated"
internal const val expirationDateKey = "expiration_date"
internal const val dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX"
internal val dateFormatter = SimpleDateFormat(dateFormat)

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
          expirationDate = dateFormatter.parse(jsonObject.getString(expirationDateKey)),
          createdAt = dateFormatter.parse(createdDate),
          updatedAt = dateFormatter.parse(updatedDate),
          challengeDetails = toChallengeDetails(details),
          hiddenDetails = jsonObject.getString(hiddenDetailsKey),
          status = ChallengeStatus.values().find { it.name == jsonObject.getString(statusKey) }
              ?: Expired
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
    val fields = detailsJson.optJSONArray(fieldsKey)?.takeIf { it.length() > 0 }
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
        ?.let { dateFormatter.parse(it) }
    return ChallengeDetails(message, fields, date)
  }
}