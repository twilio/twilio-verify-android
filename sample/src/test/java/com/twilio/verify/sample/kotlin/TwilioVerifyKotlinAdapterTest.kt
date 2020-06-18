package com.twilio.verify.sample.kotlin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.TwilioVerify
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.models.Challenge
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.FactorType.PUSH
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.sample.IdlingResource
import com.twilio.verify.sample.TwilioVerifyAdapter
import com.twilio.verify.sample.model.CreateFactorData
import com.twilio.verify.sample.model.EnrollmentResponse
import com.twilio.verify.sample.networking.SampleBackendAPIClient
import com.twilio.verify.sample.push.NewChallenge
import com.twilio.verify.sample.push.VerifyEventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Copyright (c) 2020, Twilio Inc.
 */
@RunWith(RobolectricTestRunner::class)
class TwilioVerifyKotlinAdapterTest {

  private lateinit var twilioVerifyAdapter: TwilioVerifyAdapter
  private val twilioVerify: TwilioVerify = mock()
  private val sampleBackendAPIClient: SampleBackendAPIClient = mock()
  private val verifyEventBus: VerifyEventBus = mock()
  private val idlingResource = IdlingResource()

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    twilioVerifyAdapter =
      TwilioVerifyKotlinAdapter(
          applicationContext = context, twilioVerify = twilioVerify,
          sampleBackendAPIClient = sampleBackendAPIClient, verifyEventBus = verifyEventBus
      )
  }

  @Test
  fun `Create factor with invalid JWE should return exception`() {
    val expectedException: RuntimeException = mock()
    val createFactorData = CreateFactorData("identity", "factorName", "pushToken")
    val mockCall: Call<EnrollmentResponse> = mock {
      argumentCaptor<(Callback<EnrollmentResponse>)>().apply {
        on { enqueue(capture()) }.thenThrow(expectedException)
      }
    }
    whenever(sampleBackendAPIClient.enrollment(createFactorData.identity)).thenReturn(mockCall)
    idlingResource.startOperation()
    twilioVerifyAdapter.createFactor(createFactorData, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with an error should return exception`() {
    val expectedException: TwilioVerifyException = mock()
    val createFactorData = CreateFactorData("identity", "factorName", "pushToken")
    val mockCall: Call<EnrollmentResponse> = mock { mockCall ->
      argumentCaptor<(Callback<EnrollmentResponse>)>().apply {
        on { enqueue(capture()) }.then {
          firstValue.onResponse(
              mockCall, Response.success(EnrollmentResponse("jwe", "serviceSid", "identity", PUSH.factorTypeName))
          )
        }
      }
    }
    whenever(sampleBackendAPIClient.enrollment(createFactorData.identity)).thenReturn(mockCall)
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(twilioVerify.createFactor(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.createFactor(createFactorData, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with valid JWE and Push type should return factor verified`() {
    val expectedFactor: Factor = mock() {
      on { type } doReturn PUSH
      on { sid } doReturn "factorSid"
    }
    val createFactorData = CreateFactorData("identity", "factorName", "pushToken")
    val mockCall: Call<EnrollmentResponse> = mock { mockCall ->
      argumentCaptor<(Callback<EnrollmentResponse>)>().apply {
        on { enqueue(capture()) }.then {
          firstValue.onResponse(
              mockCall, Response.success(EnrollmentResponse("jwe", "serviceSid", "identity", PUSH.factorTypeName))
          )
        }
      }
    }
    whenever(sampleBackendAPIClient.enrollment(createFactorData.identity)).thenReturn(mockCall)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(twilioVerify.createFactor(any(), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    val expectedVerifiedFactor: Factor = mock() {
      on { status } doReturn Verified
    }
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(twilioVerify.verifyFactor(any(), capture(), any())).then {
        firstValue.invoke(expectedVerifiedFactor)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.createFactor(createFactorData, { factor ->
      assertEquals(expectedVerifiedFactor, factor)
      assertEquals(expectedVerifiedFactor.status, Verified)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with success response should return factor`() {
    val expectedFactor: Factor = mock() {
      on { sid } doReturn "factorSid"
    }
    val verifyFactorInput = VerifyPushFactorInput(expectedFactor.sid)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(twilioVerify.verifyFactor(any(), capture(), any())).then {
        firstValue.invoke(expectedFactor)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.verifyFactor(verifyFactorInput, { factor ->
      assertEquals(expectedFactor, factor)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with an error should return exception`() {
    val verifyFactorInput = VerifyPushFactorInput("factorSid")
    val expectedException: TwilioVerifyException = mock()

    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(twilioVerify.verifyFactor(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.verifyFactor(verifyFactorInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenge with success response should send NewChallenge event`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    twilioVerifyAdapter.showChallenge(challengeSid, factorSid)
    verify(verifyEventBus).send(check { challengeEvent ->
      assertTrue(challengeEvent is NewChallenge)
      assertEquals(challengeSid, (challengeEvent as NewChallenge).challengeSid)
    })
  }

  @Test
  fun `Get challenge with success response should return challenge`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    val expectedChallenge: Challenge = mock()
    argumentCaptor<(Challenge) -> Unit>().apply {
      whenever(twilioVerify.getChallenge(eq(challengeSid), eq(factorSid), capture(), any())).then {
        firstValue.invoke(expectedChallenge)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.getChallenge(challengeSid, factorSid, { challenge ->
      assertEquals(expectedChallenge, challenge)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenge with an error response should return exception`() {
    val challengeSid = "challengeSid"
    val factorSid = "factorSid"
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(twilioVerify.getChallenge(eq(challengeSid), eq(factorSid), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.getChallenge(challengeSid, factorSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with success response should call success callback`() {
    val updateChallengeInput = UpdatePushChallengeInput("factorSid", "challengeSid", Approved)

    argumentCaptor<() -> Unit>().apply {
      whenever(twilioVerify.updateChallenge(eq(updateChallengeInput), capture(), any())).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.updateChallenge(updateChallengeInput, {
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with an error should return exception`() {
    val updateChallengeInput = UpdatePushChallengeInput("factorSid", "challengeSid", Approved)
    val expectedException: TwilioVerifyException = mock()

    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(twilioVerify.updateChallenge(eq(updateChallengeInput), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerifyAdapter.updateChallenge(updateChallengeInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }
}