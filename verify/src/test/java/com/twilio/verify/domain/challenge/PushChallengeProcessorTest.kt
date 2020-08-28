/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.challenge

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.data.jwt.JwtGenerator
import com.twilio.verify.domain.challenge.models.FactorChallenge
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Denied
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import org.json.JSONException
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
class PushChallengeProcessorTest {

  private val challengeProvider: ChallengeProvider = mock()
  private val jwtGenerator: JwtGenerator = mock()
  private val pushChallengeProcessor = PushChallengeProcessor(challengeProvider, jwtGenerator)
  private val idlingResource = IdlingResource()

  @Test
  fun `Get challenge with a valid challenge should call success`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val expectedChallenge: FactorChallenge = mock()
    idlingResource.startOperation()
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(expectedChallenge)
      }
    }
    pushChallengeProcessor.get(
      sid, factor,
      { challenge ->
        assertEquals(expectedChallenge, challenge)
        idlingResource.operationFinished()
      },
      { exception ->
        fail(exception.message)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenge with an exception should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val expectedException: TwilioVerifyException = mock()
    idlingResource.startOperation()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    pushChallengeProcessor.get(
      sid, factor,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with valid data should call success`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(),
      response.keys()
        .asSequence()
        .toList(),
      response
    ).apply { this.factor = factor }
    val updatedChallenge: FactorChallenge = mock()
    val alias = "alias"
    val verifyJwt = "verifyJwt"
    val status = Approved
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(
        challengeProvider.update(
          eq(challenge),
          eq(verifyJwt),
          capture(),
          any()
        )
      ).then {
        firstValue.invoke(updatedChallenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(jwtGenerator.generateJWT(any(), any(), any())).thenReturn(verifyJwt)
    whenever(updatedChallenge.status).thenReturn(status)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, status,
      {
        idlingResource.operationFinished()
      },
      { exception ->
        fail(exception.message)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with valid data should generate correct signature`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val accountSid = "accountSid"
    val serviceSid = "serviceSid"
    val identity = "identity"
    val hiddenDetails = "hiddenDetails"
    val newStatus = Denied
    val status = Pending
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
      put(hiddenDetailsKey, hiddenDetails)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), hiddenDetails, factorSid, status, mock(), mock(), mock(),
      response.keys()
        .asSequence()
        .toList(),
      response
    ).apply { this.factor = factor }
    val updatedChallenge: FactorChallenge = mock()
    val alias = "alias"
    val verifyJwt = "verifyJwt"
    whenever(factor.sid).thenReturn(factorSid)
    whenever(factor.accountSid).thenReturn(accountSid)
    whenever(factor.serviceSid).thenReturn(serviceSid)
    whenever(factor.identity).thenReturn(identity)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(
        challengeProvider.update(
          eq(challenge),
          eq(verifyJwt),
          capture(),
          any()
        )
      ).then {
        firstValue.invoke(updatedChallenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(jwtGenerator.generateJWT(any(), any(), any())).thenReturn(verifyJwt)
    whenever(updatedChallenge.status).thenReturn(newStatus)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, newStatus,
      {
        verify(jwtGenerator).generateJWT(
          check {
            assertEquals(alias, it.alias)
          },
          any(), any()
        )
        idlingResource.operationFinished()
      },
      { exception ->
        fail(exception.message)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with invalid challenge should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val challenge: Challenge = mock()
    val alias = "alias"
    whenever(factor.keyPairAlias).thenReturn(alias)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
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
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, pushFactor, Approved,
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
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
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
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with no signature fields for push factor should call error`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(), response = response
    ).apply { this.factor = factor }
    val alias = "alias"
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with empty signature fields for push factor should call error`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(), emptyList(), response
    ).apply { this.factor = factor }
    val alias = "alias"
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with no response for push factor should call error`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(),
      response.keys()
        .asSequence()
        .toList()
    ).apply { this.factor = factor }
    val alias = "alias"
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with empty response for push factor should call error`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(),
      response.keys()
        .asSequence()
        .toList(),
      JSONObject()
    ).apply { this.factor = factor }
    val alias = "alias"
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with response without signature field for push factor should call error`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(),
      response.keys()
        .asSequence()
        .toList() + "test",
      response
    ).apply { this.factor = factor }
    val alias = "alias"
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is JSONException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with error generating signature for push factor should call error`() {
    val sid = "sid123"
    val factorSid = "sid123"
    val factor: PushFactor = mock()
    val response = JSONObject().apply {
      put(sidKey, sid)
      put(factorSidKey, factorSid)
    }
    val challenge: FactorChallenge = FactorChallenge(
      sid, mock(), "", factorSid, Pending, mock(), mock(), mock(),
      response.keys()
        .asSequence()
        .toList(),
      response
    ).apply { this.factor = factor }
    val alias = "alias"
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(jwtGenerator.generateJWT(any(), any(), any())).thenThrow(
      IllegalArgumentException("Invalid signature")
    )
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, Approved,
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
  fun `Update challenge with updated challenge's status different than sent should call error`() {
    val sid = "sid123"
    val factor: PushFactor = mock()
    val challenge: FactorChallenge = mock()
    val updatedChallenge: FactorChallenge = mock()
    val alias = "alias"
    val verifyJwt = "verifyJwt"
    val status = Approved
    whenever(challenge.status).thenReturn(Pending)
    whenever(challenge.factor).thenReturn(factor)
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeProvider.get(eq(sid), eq(factor), capture(), any())).then {
        firstValue.invoke(challenge)
      }
    }
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(
        challengeProvider.update(
          eq(challenge),
          eq(verifyJwt),
          capture(),
          any()
        )
      ).then {
        firstValue.invoke(updatedChallenge)
      }
    }
    whenever(factor.keyPairAlias).thenReturn(alias)
    whenever(jwtGenerator.generateJWT(any(), any(), any())).thenReturn(verifyJwt)
    whenever(updatedChallenge.status).thenReturn(Denied)
    idlingResource.startOperation()
    pushChallengeProcessor.update(
      sid, factor, status,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }
}
