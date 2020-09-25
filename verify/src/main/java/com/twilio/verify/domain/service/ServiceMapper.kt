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

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.sidKey
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.service.models.FactorService
import com.twilio.verify.models.Service
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException

internal class ServiceMapper {

  @Throws(TwilioVerifyException::class)
  fun fromApi(jsonObject: JSONObject): Service {
    try {
      val sid = jsonObject.getString(sidKey)
      val accountSid = jsonObject.getString(accountSidKey)
      val friendlyName = jsonObject.getString(friendlyNameKey)
      val createdDate = jsonObject.getString(createdDateKey)
      val updatedDate = jsonObject.getString(updatedDateKey)
      return FactorService(
        sid = sid, createdDate = fromRFC3339Date(createdDate),
        updatedDate = fromRFC3339Date(updatedDate),
        friendlyName = friendlyName, accountSid = accountSid
      )
    } catch (e: JSONException) {
      throw TwilioVerifyException(e, MapperError)
    } catch (e: ParseException) {
      throw TwilioVerifyException(e, MapperError)
    }
  }
}
