package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
import androidx.test.core.app.ApplicationProvider
import com.twilio.verify.networking.HttpMethod.DELETE
import com.twilio.verify.networking.HttpMethod.GET
import com.twilio.verify.networking.HttpMethod.POST
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestHelperTest {

  @Test
  fun `CommonHeaders with Post http method should contain 4 pairs`() {
    val authorization = BasicAuthorization("accountSid", "authToken")
    val requestHelper = RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val commonHeaders = requestHelper.commonHeaders(POST)
    assertEquals(4, commonHeaders.size)
    assertEquals(MediaTypeValue.JSON.type, commonHeaders[MediaTypeHeader.ACCEPT.type])
    assertEquals(MediaTypeValue.URL_ENCODED.type, commonHeaders[MediaTypeHeader.CONTENT_TYPE.type])
    assertTrue(commonHeaders.containsKey(userAgent))
    assertTrue(commonHeaders.containsKey(AuthorizationHeader))
  }

  @Test
  fun `CommonHeaders with Get http method should contain 4 pairs`() {
    val authorization = BasicAuthorization("accountSid", "authToken")
    val requestHelper = RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val commonHeaders = requestHelper.commonHeaders(GET)
    assertEquals(4, commonHeaders.size)
    assertEquals(MediaTypeValue.URL_ENCODED.type, commonHeaders[MediaTypeHeader.ACCEPT.type])
    assertEquals(MediaTypeValue.URL_ENCODED.type, commonHeaders[MediaTypeHeader.CONTENT_TYPE.type])
    assertTrue(commonHeaders.containsKey(userAgent))
    assertTrue(commonHeaders.containsKey(AuthorizationHeader))
  }

  @Test
  fun `CommonHeaders with Delete http method should contain 4 pairs`() {
    val authorization = BasicAuthorization("username", "password")
    val requestHelper = RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val commonHeaders = requestHelper.commonHeaders(DELETE)
    assertEquals(4, commonHeaders.size)
    assertEquals(MediaTypeValue.JSON.type, commonHeaders[MediaTypeHeader.ACCEPT.type])
    assertEquals(MediaTypeValue.URL_ENCODED.type, commonHeaders[MediaTypeHeader.CONTENT_TYPE.type])
    assertTrue(commonHeaders.containsKey(userAgent))
    assertTrue(commonHeaders.containsKey(AuthorizationHeader))
  }
}
