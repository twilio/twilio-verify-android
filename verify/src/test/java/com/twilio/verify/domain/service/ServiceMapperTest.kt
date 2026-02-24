package com.twilio.verify.domain.service

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MapperError
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.CREATED_DATE_KEY
import com.twilio.verify.domain.challenge.SID_KEY
import com.twilio.verify.domain.challenge.UPDATED_DATE_KEY
import com.twilio.verify.domain.factor.ACCOUNT_SID_KEY
import com.twilio.verify.domain.factor.FRIENDLY_NAME_KEY
import org.hamcrest.Matchers
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.ParseException

/*
 * Copyright (c) 2020, Twilio Inc.
 */

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ServiceMapperTest {
  private val serviceMapper = ServiceMapper()

  @get:Rule
  val exceptionRule: ExpectedException = ExpectedException.none()

  @Test
  fun `Map a valid response from API should return a service`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(ACCOUNT_SID_KEY, "accountSid123")
        put(FRIENDLY_NAME_KEY, "friendlyName")
        put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
      }
    val service = serviceMapper.fromApi(jsonObject)
    assertEquals(jsonObject.getString(SID_KEY), service.sid)
    assertEquals(jsonObject.getString(FRIENDLY_NAME_KEY), service.friendlyName)
    assertEquals(fromRFC3339Date(jsonObject.getString(CREATED_DATE_KEY)), service.createdDate)
    assertEquals(fromRFC3339Date(jsonObject.getString(UPDATED_DATE_KEY)), service.updatedDate)
  }

  @Test
  fun `Map an invalid response from API should throw an error`() {
    val jsonObject =
      JSONObject().apply {
        put(ACCOUNT_SID_KEY, "accountSid123")
        put(FRIENDLY_NAME_KEY, "friendlyName")
        put(CREATED_DATE_KEY, "2020-02-19T16:39:57-08:00")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
      }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    serviceMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API with invalid created date should throw an error`() {
    val jsonObject =
      JSONObject().apply {
        put(SID_KEY, "sid123")
        put(ACCOUNT_SID_KEY, "accountSid123")
        put(FRIENDLY_NAME_KEY, "friendlyName")
        put(CREATED_DATE_KEY, "19-02-2020")
        put(UPDATED_DATE_KEY, "2020-02-21T18:39:57-08:00")
      }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf<Throwable>(ParseException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MapperError))
    serviceMapper.fromApi(jsonObject)
  }
}
