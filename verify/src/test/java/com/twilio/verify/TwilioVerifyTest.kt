/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.api.Action.DELETE
import com.twilio.verify.api.Action.READ
import com.twilio.verify.api.Action.UPDATE
import com.twilio.verify.api.AuthenticationTokenException
import com.twilio.verify.domain.challenge.challengesKey
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.dateKey
import com.twilio.verify.domain.challenge.detailsKey
import com.twilio.verify.domain.challenge.entitySidKey
import com.twilio.verify.domain.challenge.expirationDateKey
import com.twilio.verify.domain.challenge.factorSidKey
import com.twilio.verify.domain.challenge.fieldsKey
import com.twilio.verify.domain.challenge.hiddenDetailsKey
import com.twilio.verify.domain.challenge.key
import com.twilio.verify.domain.challenge.labelKey
import com.twilio.verify.domain.challenge.messageKey
import com.twilio.verify.domain.challenge.metaKey
import com.twilio.verify.domain.challenge.nextPageKey
import com.twilio.verify.domain.challenge.pageKey
import com.twilio.verify.domain.challenge.pageSizeKey
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.challenge.valueKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.configKey
import com.twilio.verify.domain.factor.credentialSidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.sharedPreferencesName
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.domain.factor.statusKey
import com.twilio.verify.models.ChallengeListInput
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.models.UpdatePushFactorInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.NetworkProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreSpi
import java.security.Provider
import java.security.Security
import java.security.cert.Certificate
import java.util.Date
import java.util.Enumeration

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [TestKeystore::class])
class TwilioVerifyTest {

  private lateinit var factor: Factor
  private val networkProvider: NetworkProvider = mock()
  private val authentication: Authentication = mock()
  private lateinit var twilioVerify: TwilioVerify
  private lateinit var provider: Provider
  private val providerName = "AndroidKeyStore"
  private val idlingResource = IdlingResource()
  private lateinit var context: Context
  private lateinit var preferences: SharedPreferences
  private val factorIdentity = "factor identity"
  private val factorServiceSid = "factor service Sid"

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    provider = object : Provider(
        providerName, 1.0, "Fake KeyStore which is used for Robolectric tests"
    ) {
      init {
        put(
            "KeyStore.$providerName",
            "com.twilio.verify.KeyStoreMock"
        )
      }
    }
    Security.insertProviderAt(provider, 0)
    preferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    twilioVerify =
      TwilioVerify.Builder(context, authentication)
          .networkProvider(networkProvider)
          .build()
  }

  @After
  fun tearDown() {
    preferences.edit()
        .clear()
        .apply()
  }

  @Test
  fun `Create a factor should call success`() {
    val jsonObject = JSONObject()
        .put(sidKey, "sid123")
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(statusKey, Unverified.value)
        .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val jwt =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
          "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
          "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
          "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
          "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
          "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
          "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
          "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorInput =
      PushFactorInput("friendly name", factorServiceSid, factorIdentity, "pushToken", jwt)
    idlingResource.startOperation()
    twilioVerify.createFactor(factorInput, { factor ->
      assertEquals(jsonObject.getString(sidKey), factor.sid)
      assertTrue(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update a factor with auth token successfully generated should call success`() {
    val sid = "sid"
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(statusKey, Verified.value)
        .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(sid),
              challengeSid = eq(null),
              serviceSid = eq(factorServiceSid),
              action = eq(UPDATE),
              success = capture(),
              error = any()
          )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    val updatePushFactorInput = UpdatePushFactorInput(sid, "pushToken")
    idlingResource.startOperation()
    twilioVerify.updateFactor(updatePushFactorInput, { factor ->
      assertEquals(jsonObject.getString(sidKey), factor.sid)
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update a factor with auth token generation failed should call error`() {
    val sid = "sid"
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
        .put(sidKey, sid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(statusKey, Verified.value)
        .put(credentialSidKey, "credential sid")
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(sid),
              challengeSid = eq(null),
              serviceSid = eq(factorServiceSid),
              action = eq(UPDATE),
              success = any(),
              error = capture()
          )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    val updatePushFactorInput = UpdatePushFactorInput(sid, "pushToken")
    idlingResource.startOperation()
    twilioVerify.updateFactor(updatePushFactorInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify a factor with auth token successfully generated should call success`() {
    val sid = "sid"
    val verifyFactorInput = VerifyPushFactorInput(sid)
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
        .put(sidKey, sid)
        .put(statusKey, Verified.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(sid),
              challengeSid = eq(null),
              serviceSid = eq(factorServiceSid),
              action = eq(UPDATE),
              success = capture(),
              error = any()
          )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    twilioVerify.verifyFactor(verifyFactorInput, { factor ->
      assertEquals(jsonObject.getString(sidKey), factor.sid)
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify a factor with auth token generation failed should call error`() {
    val sid = "sid"
    val verifyFactorInput = VerifyPushFactorInput(sid)
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
        .put(sidKey, sid)
        .put(statusKey, Verified.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(sid),
              challengeSid = eq(null),
              serviceSid = eq(factorServiceSid),
              action = eq(UPDATE),
              success = any(),
              error = capture()
          )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerify.verifyFactor(verifyFactorInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenge with auth token successfully generated should call success`() {
    val factorSid = "factorSid"
    val challengeSid = "challengeSid"
    val status = Approved
    createFactor(factorSid, Verified)
    val jsonObject = JSONObject().apply {
      put(com.twilio.verify.domain.challenge.sidKey, challengeSid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(entitySidKey, "entitySid")
      put(com.twilio.verify.domain.challenge.statusKey, status.value)
      put(detailsKey, JSONObject().apply {
        put(messageKey, "message123")
        put(fieldsKey, JSONObject().apply {
          put(labelKey, "label123")
          put(valueKey, "value123")
        })
        put(dateKey, "2020-02-19T16:39:57-08:00")
      }).toString()
      put(hiddenDetailsKey, JSONObject().apply {
        put("key1", "value1")
      }.toString())
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(jsonObject.toString())
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(factorSid),
              challengeSid = eq(challengeSid),
              serviceSid = eq(factorServiceSid),
              action = eq(READ),
              success = capture(),
              error = any()
          )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    twilioVerify.getChallenge(challengeSid, factorSid, { challenge ->
      assertEquals(challengeSid, challenge.sid)
      assertEquals(status.value, challenge.status.value)
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get challenge with auth token generation failed should call error`() {
    val factorSid = "factorSid"
    val challengeSid = "challengeSid"
    val status = Approved
    createFactor(factorSid, Verified)
    val jsonObject = JSONObject().apply {
      put(com.twilio.verify.domain.challenge.sidKey, challengeSid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(entitySidKey, "entitySid")
      put(com.twilio.verify.domain.challenge.statusKey, status.value)
      put(detailsKey, JSONObject().apply {
        put(messageKey, "message123")
        put(fieldsKey, JSONObject().apply {
          put(labelKey, "label123")
          put(valueKey, "value123")
        })
        put(dateKey, "2020-02-19T16:39:57-08:00")
      }).toString()
      put(hiddenDetailsKey, JSONObject().apply {
        put("key1", "value1")
      }.toString())
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(jsonObject.toString())
      }
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(factorSid),
              challengeSid = eq(challengeSid),
              serviceSid = eq(factorServiceSid),
              action = eq(READ),
              success = any(),
              error = capture()
          )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerify.getChallenge(challengeSid, factorSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with auth token successfully generated should call success`() {
    val factorSid = "factorSid"
    createFactor(factorSid, Verified)
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengeInput = UpdatePushChallengeInput(factorSid, challengeSid, status)
    fun challengeResponse(status: ChallengeStatus): String = JSONObject().apply {
      put(com.twilio.verify.domain.challenge.sidKey, challengeSid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(entitySidKey, "entitySid")
      put(com.twilio.verify.domain.challenge.statusKey, status.value)
      put(detailsKey, JSONObject().apply {
        put(messageKey, "message123")
        put(fieldsKey, JSONArray().apply {
          put(0, JSONObject().apply {
            put(labelKey, "label123")
            put(valueKey, "value123")
          })
        })
        put(dateKey, "2020-02-19T16:39:57-08:00")
      }.toString())
      put(hiddenDetailsKey, JSONObject().apply {
        put("key1", "value1")
      }.toString())
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }.toString()

    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        when (allValues.size) {
          1 -> lastValue.invoke(challengeResponse(Pending))
          2 -> lastValue.invoke("")
          3 -> lastValue.invoke(challengeResponse(status))
          else -> fail()
        }

      }
    }

    argumentCaptor<(String) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(factorSid),
              challengeSid = eq(challengeSid),
              serviceSid = eq(factorServiceSid),
              action = any(),
              success = capture(),
              error = any()
          )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    twilioVerify.updateChallenge(updateChallengeInput, {
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update challenge with auth token generation failed should call error`() {
    val factorSid = "factorSid"
    createFactor(factorSid, Verified)
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengeInput = UpdatePushChallengeInput(factorSid, challengeSid, status)
    fun challengeResponse(status: ChallengeStatus): String = JSONObject().apply {
      put(com.twilio.verify.domain.challenge.sidKey, challengeSid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(entitySidKey, "entitySid")
      put(com.twilio.verify.domain.challenge.statusKey, status.value)
      put(detailsKey, JSONObject().apply {
        put(messageKey, "message123")
        put(fieldsKey, JSONArray().apply {
          put(0, JSONObject().apply {
            put(labelKey, "label123")
            put(valueKey, "value123")
          })
        })
        put(dateKey, "2020-02-19T16:39:57-08:00")
      }.toString())
      put(hiddenDetailsKey, JSONObject().apply {
        put("key1", "value1")
      }.toString())
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }.toString()

    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        when (allValues.size) {
          1 -> lastValue.invoke(challengeResponse(Pending))
          2 -> lastValue.invoke("")
          3 -> lastValue.invoke(challengeResponse(status))
          else -> fail()
        }

      }
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(factorSid),
              challengeSid = eq(challengeSid),
              serviceSid = eq(factorServiceSid),
              action = eq(READ),
              success = any(),
              error = capture()
          )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerify.updateChallenge(updateChallengeInput, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all factors should call success`() {
    val factors = mutableListOf<Factor>()
    repeat(3) {
      createFactor("factor$it", Verified)
      factors.add(factor)
    }
    idlingResource.startOperation()
    twilioVerify.getAllFactors({
      assertEquals(factors.size, it.size)
      for (factor in factors) {
        assertNotNull(it.firstOrNull { it.sid == factor.sid })
      }
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get service with auth token successfully generated should call success`() {
    val factorSid = "factor sid"
    createFactor(factorSid, Verified)
    val serviceSid = factorServiceSid
    val jsonObject = JSONObject().apply {
      put(sidKey, serviceSid)
      put(friendlyNameKey, "friendlyName")
      put(accountSidKey, "accountSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(factorIdentity),
          factorSid = eq(null),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(READ),
          success = capture(),
          error = any()
        )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    twilioVerify.getService(serviceSid, { service ->
      assertEquals(jsonObject.getString(sidKey), service.sid)
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get service with auth token generation failed should call error`() {
    val factorSid = "factor sid"
    createFactor(factorSid, Verified)
    val serviceSid = factorServiceSid
    val jsonObject = JSONObject().apply {
      put(sidKey, serviceSid)
      put(friendlyNameKey, "friendlyName")
      put(accountSidKey, "accountSid123")
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(factorIdentity),
          factorSid = eq(null),
          challengeSid = eq(null),
          serviceSid = eq(factorServiceSid),
          action = eq(READ),
          success = any(),
          error = capture()
        )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerify.getService(serviceSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all challenges with auth token successfully generated should call success`() {
    val factorSid = "factorSid123"
    createFactor(factorSid, Verified)
    val challengeListInput = ChallengeListInput(factorSid, 1, null, null)
    val expectedChallenges = JSONArray(
        listOf(
            challengeJSONObject("sid123", factorSid),
            challengeJSONObject("sid456", factorSid)
        )
    )
    val expectedMetadata = metaJSONObject()
    val jsonObject = JSONObject().apply {
      put(challengesKey, expectedChallenges)
      put(metaKey, expectedMetadata)
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(factorIdentity),
          factorSid = eq(factorSid),
          challengeSid = eq("*"),
          serviceSid = eq(factorServiceSid),
          action = eq(READ),
          success = capture(),
          error = any()
        )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    twilioVerify.getAllChallenges(challengeListInput, { list ->
      val firstChallenge = list.challenges.first()
      val secondChallenge = list.challenges.last()

      assertEquals(expectedChallenges.length(), list.challenges.size)
      assertEquals(factorSid, firstChallenge.factorSid)
      assertEquals(expectedChallenges.getJSONObject(0).getString(sidKey), firstChallenge.sid)
      assertEquals(factorSid, secondChallenge.factorSid)
      assertEquals(expectedChallenges.getJSONObject(1).getString(sidKey), secondChallenge.sid)

      assertEquals(expectedMetadata.getString(key), list.metadata.key)
      assertEquals(expectedMetadata.getString(nextPageKey), list.metadata.nextPageURL)
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Get all challenges with auth token generation failed should call error`() {
    val factorSid = "factorSid123"
    createFactor(factorSid, Verified)
    val challengeListInput = ChallengeListInput(factorSid, 1, null, null)
    val expectedChallenges = JSONArray(
        listOf(
            challengeJSONObject("sid123", factorSid),
            challengeJSONObject("sid456", factorSid)
        )
    )
    val expectedMetadata = metaJSONObject()
    val jsonObject = JSONObject().apply {
      put(challengesKey, expectedChallenges)
      put(metaKey, expectedMetadata)
    }
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
        authentication.generateJWE(
          identity = eq(factorIdentity),
          factorSid = eq(factorSid),
          challengeSid = eq("*"),
          serviceSid = eq(factorServiceSid),
          action = eq(READ),
          success = any(),
          error = capture()
        )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerify.getAllChallenges(challengeListInput, { list ->
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Delete factor with auth token successfully generated should call success`() {
    val factorSid = "factorSid123"
    createFactor(factorSid, Verified)
    assertTrue(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
    assertTrue(preferences.contains(factorSid))
    argumentCaptor<(String) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(factorSid),
              challengeSid = eq(null),
              serviceSid = eq(factorServiceSid),
              action = eq(DELETE),
              success = capture(),
              error = any()
          )
      ).then {
        lastValue.invoke("authToken")
      }
    }
    idlingResource.startOperation()
    twilioVerify.deleteFactor(factorSid, {
      assertFalse(preferences.contains(factorSid))
      assertFalse(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Delete factor with auth token generation failed should call error`() {
    val factorSid = "factorSid123"
    createFactor(factorSid, Verified)
    assertTrue(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
    assertTrue(preferences.contains(factorSid))
    val expectedException: Exception = mock()
    argumentCaptor<(Exception) -> Unit>().apply {
      whenever(
          authentication.generateJWE(
              identity = eq(factorIdentity),
              factorSid = eq(factorSid),
              challengeSid = eq(null),
              serviceSid = eq(factorServiceSid),
              action = eq(DELETE),
              success = any(),
              error = capture()
          )
      ).then {
        lastValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    twilioVerify.deleteFactor(factorSid, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(TwilioVerifyException::class, exception::class)
      assertEquals(AuthenticationTokenException::class, exception.cause!!::class)
      assertEquals(expectedException, exception.cause!!.cause)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  private fun createFactor(
    factorSid: String,
    status: FactorStatus
  ) {
    val jsonObject = JSONObject()
        .put(sidKey, factorSid)
        .put(friendlyNameKey, "factor name")
        .put(accountSidKey, "accountSid123")
        .put(configKey, JSONObject().put(credentialSidKey, "credential sid"))
        .put(statusKey, status.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val jwt =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
          "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
          "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
          "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
          "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
          "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
          "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
          "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorInput =
      PushFactorInput("friendly name", factorServiceSid, factorIdentity, "pushToken", jwt)
    idlingResource.startOperation()
    twilioVerify.createFactor(factorInput, { factor ->
      this.factor = factor
      assertEquals(factorSid, factor.sid)
      assertEquals(status, factor.status)
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }
}

private fun challengeJSONObject(
  sid: String,
  factorSid: String
): JSONObject {
  return JSONObject().apply {
    put(sidKey, sid)
    put(factorSidKey, factorSid)
    put(createdDateKey, "2020-02-19T16:39:57-08:00")
    put(updatedDateKey, "2020-02-21T18:39:57-08:00")
    put(com.twilio.verify.domain.challenge.statusKey, Pending.value)
    put(entitySidKey, "entitySid")
    put(detailsKey, JSONObject().apply {
      put(messageKey, "message123")
      put(fieldsKey, JSONArray().apply {
        put(0, JSONObject().apply {
          put(labelKey, "label123")
          put(valueKey, "value123")
        })
      })
      put(dateKey, "2020-02-19T16:39:57-08:00")
    }.toString())
    put(hiddenDetailsKey, JSONObject().apply {
      put("key1", "value1")
    }.toString())
    put(expirationDateKey, "2020-02-27T08:50:57-08:00")
  }
}

private fun metaJSONObject(): JSONObject {
  return JSONObject().apply {
    put(pageKey, 1)
    put(pageSizeKey, 10)
    put(nextPageKey, "next_page")
    put(key, "key")
  }
}

private val keys = mutableMapOf<String, String>()

@Implements(com.twilio.security.crypto.AndroidKeyManager::class)
class TestKeystore {
  @Implementation
  fun signer(template: SignerTemplate): Signer {
    keys[template.alias] = template.alias.hashCode()
        .toString()
    val mock: Signer = mock()
    whenever(mock.getPublic()).thenReturn(keys[template.alias]?.toByteArray())
    whenever(mock.sign(any())).thenReturn(keys[template.alias]?.toByteArray())
    return mock
  }

  @Implementation
  fun delete(alias: String) {
    keys.remove(alias)
  }
}

class KeyStoreMock : KeyStoreSpi() {
  override fun engineIsKeyEntry(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineIsCertificateEntry(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineGetCertificate(alias: String?): Certificate {
    throw NotImplementedError()
  }

  override fun engineGetCreationDate(alias: String?): Date {
    throw NotImplementedError()
  }

  override fun engineDeleteEntry(alias: String?) {
    throw NotImplementedError()
  }

  override fun engineSetKeyEntry(
    alias: String?,
    key: Key?,
    password: CharArray?,
    chain: Array<out Certificate>?
  ) {
    throw NotImplementedError()
  }

  override fun engineSetKeyEntry(
    alias: String?,
    key: ByteArray?,
    chain: Array<out Certificate>?
  ) {
    throw NotImplementedError()
  }

  override fun engineStore(
    stream: OutputStream?,
    password: CharArray?
  ) {
    throw NotImplementedError()
  }

  override fun engineSize(): Int {
    throw NotImplementedError()
  }

  override fun engineAliases(): Enumeration<String> {
    throw NotImplementedError()
  }

  override fun engineContainsAlias(alias: String?): Boolean {
    throw NotImplementedError()
  }

  override fun engineLoad(
    stream: InputStream?,
    password: CharArray?
  ) {

  }

  override fun engineGetCertificateChain(alias: String?): Array<Certificate> {
    throw NotImplementedError()
  }

  override fun engineSetCertificateEntry(
    alias: String?,
    cert: Certificate?
  ) {
    throw NotImplementedError()
  }

  override fun engineGetCertificateAlias(cert: Certificate?): String {
    throw NotImplementedError()
  }

  override fun engineGetKey(
    alias: String?,
    password: CharArray?
  ): Key {
    throw NotImplementedError()
  }

  override fun engineGetEntry(
    alias: String?,
    protParam: KeyStore.ProtectionParameter?
  ): KeyStore.Entry? {
    throw NotImplementedError()
  }
}