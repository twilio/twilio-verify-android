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

package com.twilio.verify.domain.service

import com.twilio.security.logger.Level
import com.twilio.security.logger.Logger
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.CREATED_DATE_KEY
import com.twilio.verify.domain.challenge.SID_KEY
import com.twilio.verify.domain.challenge.UPDATED_DATE_KEY
import com.twilio.verify.domain.factor.ACCOUNT_SID_KEY
import com.twilio.verify.domain.factor.FRIENDLY_NAME_KEY
import com.twilio.verify.domain.service.models.FactorService
import com.twilio.verify.models.Service
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException

internal class ServiceMapper {
  @Throws(TwilioVerifyException::class)
  fun fromApi(jsonObject: JSONObject): Service {
    try {
      val sid = jsonObject.getString(SID_KEY)
      val accountSid = jsonObject.getString(ACCOUNT_SID_KEY)
      val friendlyName = jsonObject.getString(FRIENDLY_NAME_KEY)
      val createdDate = jsonObject.getString(CREATED_DATE_KEY)
      val updatedDate = jsonObject.getString(UPDATED_DATE_KEY)
      return FactorService(
        sid = sid,
        createdDate = fromRFC3339Date(createdDate),
        updatedDate = fromRFC3339Date(updatedDate),
        friendlyName = friendlyName,
        accountSid = accountSid,
      )
    } catch (e: JSONException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      Logger.log(Level.Error, e.toString(), e)
      throw TwilioVerifyException(e, MapperError)
    }
  }
}
