/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.domain.challenge.ChallengeFacade
import com.twilio.verify.domain.factor.FactorFacade
import com.twilio.verify.domain.service.ServiceFacade
import com.twilio.verify.domain.service.models.FactorService
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeList
import com.twilio.verify.models.ChallengeListInput
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.Factor
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.Service
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.models.UpdatePushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TwilioVerifyManagerTest {

  private val factorFacade: FactorFacade = mock()
  private val challengeFacade: ChallengeFacade = mock()
  private val serviceFacade: ServiceFacade = mock()
  private val twilioVerifyManager =
    TwilioVerifyManager(factorFacade, challengeFacade, serviceFacade)
  private val idlingResource = IdlingResource()

  @Test
  fun `Create a factor should call success`() {
    val factorInput =
      PushFactorInput("friendly name", "serviceSid", "identity", "pushToken", "jwt")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.createFactor(eq(factorInput), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.createFactor(factorInput, { factor ->
      assertEquals(expectedFactor, factor)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error creating a factor should call error`() {
    val factorInput =
      PushFactorInput("friendly name", "serviceSid", "identity", "pushToken", "jwt")
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.createFactor(eq(factorInput), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.createFactor(factorInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update a factor should call success`() {
    val updatePushFactorInput = UpdatePushFactorInput("sid", "pushToken")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.updateFactor(eq(updatePushFactorInput), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.updateFactor(updatePushFactorInput, { factor ->
      assertEquals(expectedFactor, factor)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error updating a factor should call error`() {
    val updatePushFactorInput = UpdatePushFactorInput("sid", "pushToken")
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.updateFactor(eq(updatePushFactorInput), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.updateFactor(updatePushFactorInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify a factor should call success`() {
    val verifyFactorInput = VerifyPushFactorInput("sid")
    val expectedFactor: Factor = mock()
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorFacade.verifyFactor(eq(verifyFactorInput), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.verifyFactor(verifyFactorInput, { factor ->
      assertEquals(expectedFactor, factor)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error verifying a factor should call error`() {
    val verifyFactorInput = VerifyPushFactorInput("sid")
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.verifyFactor(eq(verifyFactorInput), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.verifyFactor(verifyFactorInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get factors should call success`() {
    val expectedFactors: List<Factor> = mock()
    argumentCaptor<(List<Factor>) -> Unit>().apply {
      whenever(factorFacade.getAllFactors(capture(), any())).then {
        firstValue.invoke(expectedFactors)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getAllFactors({ factors ->
      assertEquals(expectedFactors, factors)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting all factors should call error`() {
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.getAllFactors(any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getAllFactors({
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenge should call success`() {
    val sid = "sid"
    val factorSid = "factorSid"
    val expectedChallenge: Challenge = mock()
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(challengeFacade.getChallenge(eq(sid), eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedChallenge)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getChallenge(sid, factorSid, { challenge ->
      assertEquals(expectedChallenge, challenge)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting challenge should call error`() {
    val sid = "sid"
    val factorSid = "factorSid"
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(challengeFacade.getChallenge(eq(sid), eq(factorSid), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getChallenge(sid, factorSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update a challenge should call success`() {
    val factorSid = "factorSid"
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengeInput = UpdatePushChallengeInput(factorSid, challengeSid, status)
    argumentCaptor<() -> Unit>().apply {
      whenever(challengeFacade.updateChallenge(eq(updateChallengeInput), capture(), any())).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.updateChallenge(updateChallengeInput, {
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error updating a challenge should call error`() {
    val factorSid = "factorSid"
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengeInput = UpdatePushChallengeInput(factorSid, challengeSid, status)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(challengeFacade.updateChallenge(eq(updateChallengeInput), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.updateChallenge(updateChallengeInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get service should call success`() {
    val serviceSid = "sid"
    val expectedService: FactorService = mock()
    argumentCaptor<(Service) -> Unit>().apply {
      whenever(serviceFacade.getService(eq(serviceSid), capture(), any())).then {
        firstValue.invoke(expectedService)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getService(serviceSid, { challenge ->
      assertEquals(expectedService, challenge)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting service should call error`() {
    val serviceSid = "sid"
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(serviceFacade.getService(eq(serviceSid), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getService(serviceSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all challenges should call success`() {
    val challengeListInput = ChallengeListInput("factorSid", 1, null, null)
    val expectedChallengeList: ChallengeList = mock()
    argumentCaptor<(ChallengeList) -> Unit>().apply {
      whenever(challengeFacade.getAllChallenges(any(), capture(), any())).then {
        firstValue.invoke(expectedChallengeList)
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getAllChallenges(challengeListInput, { list ->
      assertEquals(expectedChallengeList, list)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error getting all challenges should call error`() {
    val challengeListInput = ChallengeListInput("factorSid", 1, null, null)
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(challengeFacade.getAllChallenges(any(), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.getAllChallenges(challengeListInput, { list ->
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Delete factor should call success`() {
    val factorSid = "factorSid"
    argumentCaptor<() -> Unit>().apply {
      whenever(factorFacade.deleteFactor(eq(factorSid), capture(), any())).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.deleteFactor(factorSid, {
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error deleting factor should call error`() {
    val factorSid = "factorSid"
    val expectedException: Exception = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorFacade.deleteFactor(eq(factorSid), any(), capture())).then {
        firstValue.invoke(TwilioVerifyException(expectedException, InputError))
      }
    }
    idlingResource.startOperation()
    twilioVerifyManager.deleteFactor(factorSid, {
      fail()
      idlingResource.operationFinished()
    }, {
      assertEquals(expectedException, it.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }
}