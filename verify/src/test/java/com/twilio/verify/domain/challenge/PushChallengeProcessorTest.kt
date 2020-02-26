/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.nhaarman.mockitokotlin2.mock
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.waitForEmpty
import com.twilio.verify.models.ChallengeStatus.Approved
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PushChallengeProcessorTest {

  private val challengeProvider: ChallengeProvider = mock()
  private val keyStorage: KeyStorage = mock()
  private val pushChallengeProcessor = PushChallengeProcessor(challengeProvider, keyStorage)
  private val counter = AtomicInteger(0)

  @Test
  fun `Update challenge with valid data should call success`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, Approved, {
      counter.decrementAndGet()
    }, { exception ->
      fail(exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }
}