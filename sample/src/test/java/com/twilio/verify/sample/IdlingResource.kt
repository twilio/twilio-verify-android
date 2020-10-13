/*
 * Copyright (c) 2020 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
