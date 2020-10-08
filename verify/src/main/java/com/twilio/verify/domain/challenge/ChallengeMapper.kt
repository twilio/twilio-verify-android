/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify.domain.challenge

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeDetails
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Expired
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Detail
import java.text.ParseException
import org.json.JSONException
import org.json.JSONObject

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
internal const val signatureFieldsHeaderSeparator = ","

internal class ChallengeMapper {
  @Throws(TwilioVerifyException::class)
  fun fromApi(
    jsonObject: JSONObject,
    signatureFieldsHeader: String? = null
  ): Challenge {
    try {
      val details = jsonObject.getJSONObject(detailsKey)
      val createdDate = jsonObject.getString(createdDateKey)
      val updatedDate = jsonObject.getString(updatedDateKey)
      val status = ChallengeStatus.values()
        .find { it.value == jsonObject.getString(statusKey) }
        ?: Expired
      val signatureFields = if (status == Pending && signatureFieldsHeader != null) {
        signatureFieldsHeader.split(signatureFieldsHeaderSeparator)
      } else {
        null
      }
      val response = if (status == Pending && signatureFields != null) {
        jsonObject
      } else {
        null
      }
      return FactorChallenge(
        sid = jsonObject.getString(sidKey), response = response,
        signatureFields = signatureFields,
        factorSid = jsonObject.getString(factorSidKey),
        expirationDate = fromRFC3339Date(jsonObject.getString(expirationDateKey)),
        createdAt = fromRFC3339Date(createdDate),
        updatedAt = fromRFC3339Date(updatedDate),
        challengeDetails = toChallengeDetails(details),
        hiddenDetails = jsonObject.optJSONObject(hiddenDetailsKey)?.let {
          it.keys().asSequence().associateWith { key -> it.getString(key) }
        },
        status = status
      )
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }

  private fun toChallengeDetails(details: JSONObject): ChallengeDetails = run {
    val message = details.getString(messageKey)
    val fields = details.optJSONArray(fieldsKey)
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
    val date = details.optString(dateKey)
      .takeIf { it.isNotEmpty() }
      ?.let { fromRFC3339Date(it) }
    return ChallengeDetails(message, fields, date)
  }
}
