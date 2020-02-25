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
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.Factor
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ChallengeRepositoryTest {

  private val apiClient: ChallengeAPIClient = mock()
  private val challengeMapper: ChallengeMapper = mock()
  private val challengeRepository = ChallengeRepository(apiClient, challengeMapper)

  @Test
  fun `Get challenge with valid response should return a challenge`() {
    val sid = "sid123"
    val factor: Factor = mock()
    val challenge: FactorChallenge = mock()
    val response = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.name)
    }
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(challengeMapper.fromApi(response)).thenReturn(challenge)
    challengeRepository.get(sid, factor, {
      assertEquals(challenge, it)
      verify(challenge).factor = factor
    }, { fail() })
  }

  @Test
  fun `No response from API getting a challenge should call error`() {
    val sid = "sid123"
    val factor: Factor = mock()
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    challengeRepository.get(sid, factor, { fail() }, { exception ->
      assertEquals(expectedException, exception)
    })
  }

  @Test
  fun `Error from mapper getting a challenge should call error`() {
    val sid = "sid123"
    val factor: Factor = mock()
    val response = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.name)
    }
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(challengeMapper.fromApi(response)).thenThrow(expectedException)
    challengeRepository.get(sid, factor, { fail() }, { exception ->
      assertEquals(expectedException, exception)
    })
  }

  @Test
  fun `Invalid challenge should call error`() {
    val sid = "sid123"
    val factor: Factor = mock()
    val challenge: Challenge = mock()
    val response = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.name)
    }
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(challengeMapper.fromApi(response)).thenReturn(challenge)
    challengeRepository.get(sid, factor, { fail() }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
    })
  }

  @Test
  fun `Update challenge with valid response should return updated challenge`() {
    val sid = "sid123"
    val payload = "payload123"
    val factor: Factor = mock()
    val challenge: FactorChallenge = mock()
    val updatedChallenge: FactorChallenge = mock()
    val response = JSONObject().apply {
      put(sidKey, "sid123")
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, ChallengeStatus.Pending.name)
    }
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.update(eq(challenge), any(), capture(), any())).then {
        firstValue.invoke()
      }
    }
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(challenge.factor).thenReturn(factor)
    whenever(challenge.sid).thenReturn(sid)
    whenever(challengeMapper.fromApi(response)).thenReturn(updatedChallenge)
    challengeRepository.update(challenge, payload, {
      assertEquals(updatedChallenge, it)
      verify(updatedChallenge).factor = factor
    }, { fail() })
  }

  @Test
  fun `Update challenge with invalid factor should call error`() {
    val payload = "payload123"
    val challenge: FactorChallenge = mock()
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.update(eq(challenge), any(), capture(), any())).then {
        firstValue.invoke()
      }
    }
    challengeRepository.update(
        challenge, payload, { fail() },
        { exception -> assertTrue(exception.cause is IllegalArgumentException) })
  }
}