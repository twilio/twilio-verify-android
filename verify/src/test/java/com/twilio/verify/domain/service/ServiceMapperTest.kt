package com.twilio.verify.domain.service

import com.twilio.verify.ErrorCodeMatcher
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.MAPPER_ERROR
import com.twilio.verify.data.fromRFC3339Date
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.sidKey
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import java.text.ParseException
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
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(accountSidKey, "accountSid123")
      put(friendlyNameKey, "friendlyName")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    val service = serviceMapper.fromApi(jsonObject)
    assertEquals(jsonObject.getString(sidKey), service.sid)
    assertEquals(jsonObject.getString(friendlyNameKey), service.friendlyName)
    assertEquals(fromRFC3339Date(jsonObject.getString(createdDateKey)), service.createdDate)
    assertEquals(fromRFC3339Date(jsonObject.getString(updatedDateKey)), service.updatedDate)
  }

  @Test
  fun `Map an invalid response from API should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(accountSidKey, "accountSid123")
      put(friendlyNameKey, "friendlyName")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(JSONException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MAPPER_ERROR))
    serviceMapper.fromApi(jsonObject)
  }

  @Test
  fun `Map a response from API with invalid created date should throw an error`() {
    val jsonObject = JSONObject().apply {
      put(sidKey, "sid123")
      put(accountSidKey, "accountSid123")
      put(friendlyNameKey, "friendlyName")
      put(createdDateKey, "19-02-2020")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    exceptionRule.expect(TwilioVerifyException::class.java)
    exceptionRule.expectCause(Matchers.instanceOf(ParseException::class.java))
    exceptionRule.expect(ErrorCodeMatcher(MAPPER_ERROR))
    serviceMapper.fromApi(jsonObject)
  }
}
