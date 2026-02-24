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

import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
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
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException

internal const val SID_KEY = "sid"
internal const val MESSAGE_KEY = "message"
internal const val DETAILS_KEY = "details"
internal const val FIELDS_KEY = "fields"
internal const val DATE_KEY = "date"
internal const val LABEL_KEY = "label"
internal const val VALUE_KEY = "value"
internal const val HIDDEN_DETAILS_KEY = "hidden_details"
internal const val FACTOR_SID_KEY = "factor_sid"
internal const val STATUS_KEY = "status"
internal const val CREATED_DATE_KEY = "date_created"
internal const val UPDATED_DATE_KEY = "date_updated"
internal const val EXPIRATION_DATE_KEY = "expiration_date"
internal const val SIGNATURE_FIELDS_HEADER_SEPARATOR = ","

internal class ChallengeMapper {
  @Throws(TwilioVerifyException::class)
  fun fromApi(
    jsonObject: JSONObject,
    SIGNATURE_FIELDS_HEADER: String? = null,
  ): Challenge {
    try {
      val details = jsonObject.getJSONObject(DETAILS_KEY)
      val createdDate = jsonObject.getString(CREATED_DATE_KEY)
      val updatedDate = jsonObject.getString(UPDATED_DATE_KEY)
      val status =
        ChallengeStatus
          .values()
          .find { it.value == jsonObject.getString(STATUS_KEY) }
          ?: Expired
      val signatureFields =
        if (status == Pending && SIGNATURE_FIELDS_HEADER != null) {
          SIGNATURE_FIELDS_HEADER.split(SIGNATURE_FIELDS_HEADER_SEPARATOR)
        } else {
          null
        }
      val response =
        if (status == Pending && signatureFields != null) {
          jsonObject
        } else {
          null
        }
      return FactorChallenge(
        sid = jsonObject.getString(SID_KEY),
        response = response,
        signatureFields = signatureFields,
        factorSid = jsonObject.getString(FACTOR_SID_KEY),
        expirationDate = fromRFC3339Date(jsonObject.getString(EXPIRATION_DATE_KEY)),
        createdAt = fromRFC3339Date(createdDate),
        updatedAt = fromRFC3339Date(updatedDate),
        challengeDetails = toChallengeDetails(details),
        hiddenDetails =
          jsonObject.optJSONObject(HIDDEN_DETAILS_KEY)?.let {
            it.keys().asSequence().associateWith { key -> it.getString(key) }
          },
        status = status,
      )
    } catch (e: JSONException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    }
  }

  private fun toChallengeDetails(details: JSONObject): ChallengeDetails =
    run {
      val message = details.getString(MESSAGE_KEY)
      val fields =
        details
          .optJSONArray(FIELDS_KEY)
          ?.takeIf { it.length() > 0 }
          ?.let {
            val fields = mutableListOf<Detail>()
            for (i in 0 until it.length()) {
              val jsonObject = it.getJSONObject(i)
              fields.add(
                Detail(
                  jsonObject.getString(LABEL_KEY),
                  jsonObject.getString(VALUE_KEY),
                ),
              )
            }
            fields
          } ?: listOf<Detail>()
      val date =
        details
          .optString(DATE_KEY)
          .takeIf { it.isNotEmpty() }
          ?.let { fromRFC3339Date(it) }
      return ChallengeDetails(message, fields, date)
    }
}
