package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
import androidx.test.core.app.ApplicationProvider
import com.twilio.verify.networking.HttpMethod.Delete
import com.twilio.verify.networking.HttpMethod.Get
import com.twilio.verify.networking.HttpMethod.Post
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
    val commonHeaders = requestHelper.commonHeaders(Post)
    assertEquals(4, commonHeaders.size)
    assertEquals(MediaTypeValue.Json.type, commonHeaders[MediaTypeHeader.Accept.type])
    assertEquals(MediaTypeValue.UrlEncoded.type, commonHeaders[MediaTypeHeader.ContentType.type])
    assertTrue(commonHeaders.containsKey(userAgent))
    assertTrue(commonHeaders.containsKey(AuthorizationHeader))
  }

  @Test
  fun `CommonHeaders with Get http method should contain 4 pairs`() {
    val authorization = BasicAuthorization("accountSid", "authToken")
    val requestHelper = RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val commonHeaders = requestHelper.commonHeaders(Get)
    assertEquals(4, commonHeaders.size)
    assertEquals(MediaTypeValue.UrlEncoded.type, commonHeaders[MediaTypeHeader.Accept.type])
    assertEquals(MediaTypeValue.UrlEncoded.type, commonHeaders[MediaTypeHeader.ContentType.type])
    assertTrue(commonHeaders.containsKey(userAgent))
    assertTrue(commonHeaders.containsKey(AuthorizationHeader))
  }

  @Test
  fun `CommonHeaders with Delete http method should contain 4 pairs`() {
    val authorization = BasicAuthorization("username", "password")
    val requestHelper = RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    val commonHeaders = requestHelper.commonHeaders(Delete)
    assertEquals(4, commonHeaders.size)
    assertEquals(MediaTypeValue.Json.type, commonHeaders[MediaTypeHeader.Accept.type])
    assertEquals(MediaTypeValue.UrlEncoded.type, commonHeaders[MediaTypeHeader.ContentType.type])
    assertTrue(commonHeaders.containsKey(userAgent))
    assertTrue(commonHeaders.containsKey(AuthorizationHeader))
  }
}
