package com.twilio.verify.networking

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/*
 * Copyright (c) 2022, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class NetworkExceptionTest {

  @Test
  fun `Error body conversion to API error`() {
    val code = "60315"
    val message = "Reached max limit of 20 push Factors associated per Entity"
    val moreInfo = "https://www.twilio.com/docs/errors/60315"
    val apiErrorJson = JSONObject().apply {
      put(CODE_KEY, code)
      put(MESSAGE_KEY, message)
      put(MORE_INFO_KEY, moreInfo)
    }
    val failureResponse = FailureResponse(400, apiErrorJson.toString(), null)
    val apiError = failureResponse.apiError
    assertEquals(code, apiError?.code)
    assertEquals(message, apiError?.message)
    assertEquals(moreInfo, apiError?.moreInfo)
  }

  @Test
  fun `Error body conversion to API error without more info`() {
    val code = "60315"
    val message = "Reached max limit of 20 push Factors associated per Entity"
    val apiErrorJson = JSONObject().apply {
      put(CODE_KEY, code)
      put(MESSAGE_KEY, message)
    }
    val failureResponse = FailureResponse(400, apiErrorJson.toString(), null)
    val apiError = failureResponse.apiError
    assertEquals(code, apiError?.code)
    assertEquals(message, apiError?.message)
  }

  @Test
  fun `Error when converting error body to API error without code`() {
    val message = "Reached max limit of 20 push Factors associated per Entity"
    val apiErrorJson = JSONObject().apply {
      put(MESSAGE_KEY, message)
    }
    val failureResponse = FailureResponse(400, apiErrorJson.toString(), null)
    val apiError = failureResponse.apiError
    assertNull(apiError)
  }
}