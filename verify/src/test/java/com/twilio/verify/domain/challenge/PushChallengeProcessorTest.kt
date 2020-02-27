/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.waitForEmpty
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus.Approved
import org.junit.Assert.assertEquals
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
  fun `Get challenge with a valid challenge should call success`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val expectedChallenge: FactorChallenge = mock()
    counter.incrementAndGet()
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(expectedChallenge)
      }
    }
    pushChallengeProcessor.get(sid, factor, { challenge ->
      assertEquals(expectedChallenge, challenge)
      counter.decrementAndGet()
    }, { exception ->
      fail(exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Get challenge with an exception should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val expectedException: TwilioVerifyException = mock()
    counter.incrementAndGet()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    pushChallengeProcessor.get(sid, factor, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertEquals(expectedException, exception)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

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