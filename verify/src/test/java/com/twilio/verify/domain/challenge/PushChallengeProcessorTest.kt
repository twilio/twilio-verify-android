/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.waitForEmpty
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Denied
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    val challenge: FactorChallenge = mock()
    val updatedChallenge: FactorChallenge = mock()
    val alias = "alias"
    val signature = "signature"
    val status = Approved
    whenever(challenge.status).thenReturn(Pending)
    whenever(challenge.factor).thenReturn(factor)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.update(eq(challenge), eq(signature), capture(), any())).then {
        firstValue.invoke(updatedChallenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(keyStorage.sign(eq(alias), any())).thenReturn(signature)
    whenever(updatedChallenge.status).thenReturn(status)
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, status, {
      counter.decrementAndGet()
    }, { exception ->
      fail(exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Update challenge with valid data should generate correct signature`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val accountSid = "accountSid"
    val serviceSid = "serviceSid"
    val entitySid = "entitySid"
    val createdDate = "createdDate"
    val updatedDate = "updatedDate"
    val details = "details"
    val hiddenDetails = "hiddenDetails"
    val status = ChallengeStatus.Denied
    val factor: PushFactor = mock()
    val challenge: FactorChallenge = FactorChallenge(
        sid, mock(), hiddenDetails, factorSid, Pending, mock(), mock(), mock(), details,
        createdDate, updatedDate
    ).apply { this.factor = factor }
    val updatedChallenge: FactorChallenge = mock()
    val alias = "alias"
    val payload = "${accountSid}${serviceSid}${entitySid}${factorSid}${sid}${createdDate}" +
        "${updatedDate}$status${details}${hiddenDetails}"
    val signature = "signature"
    whenever(factor.sid).thenReturn(factorSid)
    whenever(factor.accountSid).thenReturn(accountSid)
    whenever(factor.serviceSid).thenReturn(serviceSid)
    whenever(factor.entitySid).thenReturn(entitySid)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.update(eq(challenge), eq(signature), capture(), any())).then {
        firstValue.invoke(updatedChallenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(keyStorage.sign(eq(alias), any())).thenReturn(signature)
    whenever(updatedChallenge.status).thenReturn(status)
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, status, {
      verify(keyStorage).sign(alias, payload)
      counter.decrementAndGet()
    }, { exception ->
      fail(exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Update challenge with invalid challenge should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val challenge: Challenge = mock()
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, Approved, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Update challenge with invalid factor should call error`() {
    val sid = "sid123"
    val factor: Factor = mock()
    val pushFactor: PushFactor = mock()
    val challenge: FactorChallenge = mock()
    whenever(challenge.factor).thenReturn(factor)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(pushFactor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, pushFactor, Approved, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Update challenge with invalid alias for push factor should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val challenge: FactorChallenge = mock()
    whenever(challenge.status).thenReturn(Pending)
    whenever(challenge.factor).thenReturn(factor)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(null)
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, Approved, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Update challenge with empty alias for push factor should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val challenge: FactorChallenge = mock()
    whenever(challenge.status).thenReturn(Pending)
    whenever(challenge.factor).thenReturn(factor)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn("")
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, Approved, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Update challenge with updated challenge's status different than sent should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val challenge: FactorChallenge = mock()
    val updatedChallenge: FactorChallenge = mock()
    val alias = "alias"
    val signature = "signature"
    whenever(challenge.status).thenReturn(Pending)
    whenever(challenge.factor).thenReturn(factor)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.update(eq(challenge), eq(signature), capture(), any())).then {
        firstValue.invoke(updatedChallenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(keyStorage.sign(eq(alias), any())).thenReturn(signature)
    whenever(updatedChallenge.status).thenReturn(Denied)
    counter.incrementAndGet()
    pushChallengeProcessor.update(sid, factor, Approved, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }
}