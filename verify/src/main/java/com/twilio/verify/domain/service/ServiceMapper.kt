package com.twilio.verify.domain.service

import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.fromRFC3339Date
import com.twilio.verify.domain.challenge.sidKey
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.service.models.FactorService
import com.twilio.verify.models.Service
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException

/*
 * Copyright (c) 2020, Twilio Inc.
 */

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