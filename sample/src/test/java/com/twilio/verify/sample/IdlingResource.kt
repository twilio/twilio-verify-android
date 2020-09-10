/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample

import android.os.Looper
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertTrue
import org.robolectric.Shadows

class IdlingResource(private val counter: AtomicInteger = AtomicInteger(0)) {
  fun waitForIdle(
    waitFor: Long = 100,
    times: Int = 5
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
