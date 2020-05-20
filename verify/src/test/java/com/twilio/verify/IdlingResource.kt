/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.os.Looper
import org.junit.Assert.assertTrue
import org.robolectric.Shadows
import java.util.concurrent.atomic.AtomicInteger

class IdlingResource(private val counter: AtomicInteger = AtomicInteger(0)) {
  fun waitForIdle(
    waitFor: Long = 200,
    times: Int = 10
  ) {
    for (i in 0..times) {
      if (counter.get() > 0) {
        Thread.sleep(waitFor)
        Shadows.shadowOf(
            Looper.getMainLooper()
        )
            .idle()
      } else {
        break
      }
    }
    Shadows.shadowOf(
        Looper.getMainLooper()
    )
        .idle()
    assertTrue(counter.get() == 0)
  }

  fun startOperation() {
    counter.incrementAndGet()
  }

  fun operationFinished() {
    counter.decrementAndGet()
  }
}