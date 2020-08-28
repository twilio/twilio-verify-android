/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.Factor
import com.twilio.verify.models.UpdateChallengePayload
import com.twilio.verify.models.UpdatePushChallengePayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ChallengeFacadeTest {

  private val pushChallengeProcessor: PushChallengeProcessor = mock()
  private val factorFacade: FactorFacade = mock()
  private val repository: ChallengeRepository = mock()
  private val challengeFacade = ChallengeFacade(pushChallengeProcessor, factorFacade, repository)
  private val idlingResource = IdlingResource()

  @Test
  fun `Get a challenge with valid data should call success`() {
    val sid = "sid"
    val factorSid = "factorSid"
    val expectedFactor: PushFactor = mock()
    val expectedChallenge: Challenge = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(pushChallengeProcessor.get(eq(sid), eq(expectedFactor), capture(), any())).then {
        firstValue.invoke(expectedChallenge)
      }
    }
    idlingResource.startOperation()
    challengeFacade.getChallenge(
      sid, factorSid,
      { challenge ->
        assertEquals(expectedChallenge, challenge)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting a challenge should call error`() {
    val sid = "sid"
    val factorSid = "factorSid"
    val expectedFactor: PushFactor = mock()
    val expectedException: Exception = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(pushChallengeProcessor.get(eq(sid), eq(expectedFactor), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    challengeFacade.getChallenge(
      sid, factorSid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting the factor when getting a challenge should call error`() {
    val sid = "sid"
    val factorSid = "factorSid"
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    challengeFacade.getChallenge(
      sid, factorSid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with valid data should call success`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factorSid, challengeSid, status)
    val expectedFactor: PushFactor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    argumentCaptor<() -> Unit>().apply {
      whenever(
        pushChallengeProcessor.update(
          eq(challengeSid), eq(expectedFactor), eq(status), capture(), any()
        )
      ).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    challengeFacade.updateChallenge(
      updateChallengePayload,
      {
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error updating challenge should call error`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factorSid, challengeSid, status)
    val expectedFactor: PushFactor = mock()
    val expectedException: Exception = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
        pushChallengeProcessor.update(
          eq(challengeSid), eq(expectedFactor), eq(status), any(), capture()
        )
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    challengeFacade.updateChallenge(
      updateChallengePayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting the factor when updating a challenge should call error`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factorSid, challengeSid, status)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    challengeFacade.updateChallenge(
      updateChallengePayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with invalid update challenge input should call error`() {
    val factorSid = "factorSid"
    val updateChallengePayload: UpdateChallengePayload = mock()
    whenever(updateChallengePayload.factorSid).thenReturn(factorSid)
    val expectedFactor: PushFactor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    challengeFacade.updateChallenge(
      updateChallengePayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalArgumentException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all challenges with valid data should call success`() {
    val factorSid = "factorSid"
    val pageSize = 1
    val challengeListPayload = ChallengeListPayload(factorSid, pageSize, null, null)
    val expectedFactor: PushFactor = mock()
    val expectedChallengeList: ChallengeList = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    argumentCaptor<(ChallengeList) -> Unit>().apply {
      whenever(
        repository.getAll(eq(expectedFactor), eq(null), eq(pageSize), eq(null), capture(), any())
      ).then {
        firstValue.invoke(expectedChallengeList)
      }
    }
    idlingResource.startOperation()
    challengeFacade.getAllChallenges(
      challengeListPayload,
      { list ->
        assertEquals(expectedChallengeList, list)
        idlingResource.operationFinished()
      },
      {
        fail()
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting all challenges should call error`() {
    val factorSid = "factorSid"
    val pageSize = 1
    val challengeListPayload = ChallengeListPayload(factorSid, pageSize, null, null)
    val expectedFactor: PushFactor = mock()
    val expectedException: Exception = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(
        repository.getAll(eq(expectedFactor), eq(null), eq(pageSize), eq(null), any(), capture())
      ).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    challengeFacade.getAllChallenges(
      challengeListPayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting the factor when getting all challenges should call error`() {
    val factorSid = "factorSid"
    val pageSize = 1
    val challengeListPayload = ChallengeListPayload(factorSid, pageSize, null, null)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.getFactor(eq(factorSid), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    challengeFacade.getAllChallenges(
      challengeListPayload,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception.cause)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }
}
