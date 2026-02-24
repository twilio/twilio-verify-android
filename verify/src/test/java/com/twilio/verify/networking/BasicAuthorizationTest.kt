package com.twilio.verify.networking

/*
 * Copyright (c) 2020, Twilio Inc.
 */
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BasicAuthorizationTest {
  @Test
  fun `Authorization should return a pair with key = Authorization & value = Basic auth`() {
    val username = "username"
    val password = "password"
    val expectedBasicAuthorization = "$BASIC_AUTH dXNlcm5hbWU6cGFzc3dvcmQ="
    val authorization = BasicAuthorization(username, password)
    assertEquals(AUTHORIZATION_HEADER, authorization.header.first)
    assertEquals(expectedBasicAuthorization, authorization.header.second)
  }
}
