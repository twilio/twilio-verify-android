/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.api.ChallengeAPIClient
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeStatus.Expired
import com.twilio.verify.models.ChallengeStatus.Pending
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
  private val challengeListMapper: ChallengeListMapper = mock()
  private val challengeRepository =
    ChallengeRepository(apiClient, challengeMapper, challengeListMapper)

  @Test
  fun `Get challenge with valid response should return a challenge`() {
    val sid = "sid123"
    val factorSid = "factorSid123"
    val factor: Factor = mock()
    val challenge: FactorChallenge = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, Pending.value)
    }
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(challengeMapper.fromApi(response)).thenReturn(challenge)
    whenever(factor.sid).thenReturn(factorSid)
    whenever(challenge.factorSid).thenReturn(factorSid)
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
      put(sidKey, sid)
      put(factorSidKey, "factorSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, Pending.value)
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
    val factorSid = "factorSid123"
    val factor: Factor = mock()
    val challenge: Challenge = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, Pending.value)
    }
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factor.sid).thenReturn(factorSid)
    whenever(challenge.factorSid).thenReturn(factorSid)
    whenever(challengeMapper.fromApi(response)).thenReturn(challenge)
    challengeRepository.get(sid, factor, { fail() }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
    })
  }

  @Test
  fun `Wrong factor for challenge should call error`() {
    val sid = "sid123"
    val factorSid = "factorSid123"
    val factor: Factor = mock()
    val challenge: Challenge = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, Pending.value)
    }
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(apiClient.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(response)
      }
    }
    whenever(factor.sid).thenReturn("factorSid234")
    whenever(challenge.factorSid).thenReturn(factorSid)
    whenever(challengeMapper.fromApi(response)).thenReturn(challenge)
    challengeRepository.get(sid, factor, { fail() }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
    })
  }

  @Test
  fun `Update challenge with valid response should return updated challenge`() {
    val sid = "sid123"
    val factorSid = "factorSid123"
    val payload = "payload123"
    val factor: Factor = mock()
    val challenge: FactorChallenge = mock()
    val updatedChallenge: FactorChallenge = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(statusKey, Pending.value)
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
    whenever(factor.sid).thenReturn(factorSid)
    whenever(updatedChallenge.factorSid).thenReturn(factorSid)
    whenever(challenge.factor).thenReturn(factor)
    whenever(challenge.status).thenReturn(Pending)
    whenever(challenge.sid).thenReturn(sid)
    whenever(challengeMapper.fromApi(response)).thenReturn(updatedChallenge)
    challengeRepository.update(challenge, payload, {
      assertEquals(updatedChallenge, it)
      verify(updatedChallenge).factor = factor
    }, { fail() })
  }

  @Test
  fun `Update challenge with no factor should call error`() {
    val payload = "payload123"
    val challenge: FactorChallenge = mock()
    whenever(challenge.status).thenReturn(Pending)
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.update(eq(challenge), any(), capture(), any())).then {
        firstValue.invoke()
      }
    }
    challengeRepository.update(
        challenge, payload, { fail() },
        { exception -> assertTrue(exception.cause is IllegalArgumentException) })
  }

  @Test
  fun `Update expired challenge should call error`() {
    val payload = "payload123"
    val challenge: FactorChallenge = mock()
    whenever(challenge.status).thenReturn(Expired)
    argumentCaptor<() -> Unit>().apply {
      whenever(apiClient.update(eq(challenge), any(), capture(), any())).then {
        firstValue.invoke()
      }
    }
    challengeRepository.update(
        challenge, payload, { fail() },
        { exception -> assertTrue(exception.cause is IllegalArgumentException) })
  }

  @Test
  fun `Get challenges with valid response should return challenge list`() {
    val factor: Factor = mock()
    val status = Pending
    val pageSize = 10
    val response = JSONObject()
    val challengeList: ChallengeList = mock()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(
          apiClient.getAll(
              eq(factor), eq(status.value), eq(pageSize), anyOrNull(), capture(), any()
          )
      ).then {
        firstValue.invoke(response)
      }
    }
    whenever(challengeListMapper.fromApi(response)).thenReturn(challengeList)
    challengeRepository.getAll(factor, status, pageSize, null, {
      assertEquals(challengeList, it)
    }, { fail() })
  }

  @Test
  fun `Get challenges with error from mapper should call error`() {
    val factor: Factor = mock()
    val status = Pending
    val pageSize = 10
    val response = JSONObject()
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(JSONObject) -> Unit>().apply {
      whenever(
          apiClient.getAll(
              eq(factor), eq(status.value), eq(pageSize), anyOrNull(), capture(), any()
          )
      ).then {
        firstValue.invoke(response)
      }
    }
    whenever(challengeListMapper.fromApi(response)).thenThrow(expectedException)
    challengeRepository.getAll(factor, status, pageSize, null, {
      fail()
    }, { assertEquals(expectedException, it) })
  }
}