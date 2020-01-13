package com.twilio.verify.networking

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestHelperTest {

  @Test
  fun `CommonHeaders should contain 2 pairs with User-Agent and Authorization`() {
    val authorization = Authorization("accountSid", "authToken")
    val requestHelper = RequestHelper(ApplicationProvider.getApplicationContext(), authorization)
    assertEquals(2, requestHelper.commonHeaders.size)
    assertTrue(requestHelper.commonHeaders.containsKey(userAgent))
    assertTrue(requestHelper.commonHeaders.containsKey(AuthorizationHeader))
  }
}