/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.dateKey
import com.twilio.verify.domain.challenge.detailsKey
import com.twilio.verify.domain.challenge.entitySidKey
import com.twilio.verify.domain.challenge.expirationDateKey
import com.twilio.verify.domain.challenge.factorSidKey
import com.twilio.verify.domain.challenge.fieldsKey
import com.twilio.verify.domain.challenge.hiddenDetailsKey
import com.twilio.verify.domain.challenge.labelKey
import com.twilio.verify.domain.challenge.messageKey
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.challenge.valueKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.sharedPreferencesName
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.domain.factor.statusKey
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.PushFactorInput
import com.twilio.verify.models.UpdatePushChallengeInput
import com.twilio.verify.models.VerifyPushFactorInput
import com.twilio.verify.networking.BasicAuthorization
import com.twilio.verify.networking.NetworkProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
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

  private val networkProvider: NetworkProvider = mock()
  private val authorization = BasicAuthorization("accountSid", "authToken")
  private lateinit var twilioVerify: TwilioVerify
  private lateinit var provider: Provider
  private val providerName = "AndroidKeyStore"
  private val idlingResource = IdlingResource()
  private lateinit var context: Context

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
    twilioVerify =
      TwilioVerify.Builder(context, authorization)
          .networkProvider(networkProvider)
          .build()
  }

  @After
  fun tearDown() {
    context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        .edit()
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
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorInput = PushFactorInput("friendly name", "pushToken", jwt)
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
  fun `Verify a factor should call success`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val verifyFactorInput = VerifyPushFactorInput(sid, verificationCode)
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
        .put(sidKey, sid)
        .put(statusKey, Verified.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
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
  fun `Get challenge should call success`() {
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
  fun `Update challenge should call success`() {
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
    idlingResource.startOperation()
    twilioVerify.updateChallenge(updateChallengeInput, {
      idlingResource.operationFinished()
    }, { exception ->
      fail(exception.message)
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
        .put(statusKey, status.value)
    argumentCaptor<(String) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(jsonObject.toString())
      }
    }
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorInput = PushFactorInput("friendly name", "pushToken", jwt)
    idlingResource.startOperation()
    twilioVerify.createFactor(factorInput, { factor ->
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