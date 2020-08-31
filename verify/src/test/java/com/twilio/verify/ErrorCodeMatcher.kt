package com.twilio.verify

import com.twilio.verify.TwilioVerifyException.ErrorCode
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ErrorCodeMatcher(private val errorCode: ErrorCode) :
  TypeSafeMatcher<TwilioVerifyException>() {
  override fun describeTo(description: Description?) {
    description?.appendText("Checking errorCode ${errorCode.name}")
  }

  override fun matchesSafely(item: TwilioVerifyException?): Boolean {
    return item?.message == errorCode.message
  }
}
