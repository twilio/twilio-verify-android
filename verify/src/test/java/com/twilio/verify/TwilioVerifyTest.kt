/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.security.crypto.key.signer.Signer
import com.twilio.security.crypto.key.template.SignerTemplate
import com.twilio.verify.api.signatureFieldsHeader
import com.twilio.verify.data.jwt.FLAGS
import com.twilio.verify.data.toRFC3339Date
import com.twilio.verify.domain.challenge.challengesKey
import com.twilio.verify.domain.challenge.createdDateKey
import com.twilio.verify.domain.challenge.dateKey
import com.twilio.verify.domain.challenge.detailsKey
import com.twilio.verify.domain.challenge.expirationDateKey
import com.twilio.verify.domain.challenge.factorSidKey
import com.twilio.verify.domain.challenge.fieldsKey
import com.twilio.verify.domain.challenge.hiddenDetailsKey
import com.twilio.verify.domain.challenge.labelKey
import com.twilio.verify.domain.challenge.messageKey
import com.twilio.verify.domain.challenge.metaKey
import com.twilio.verify.domain.challenge.nextPageKey
import com.twilio.verify.domain.challenge.pageKey
import com.twilio.verify.domain.challenge.pageSizeKey
import com.twilio.verify.domain.challenge.pageTokenKey
import com.twilio.verify.domain.challenge.previousPageKey
import com.twilio.verify.domain.challenge.signatureFieldsHeaderSeparator
import com.twilio.verify.domain.challenge.updatedDateKey
import com.twilio.verify.domain.challenge.valueKey
import com.twilio.verify.domain.factor.accountSidKey
import com.twilio.verify.domain.factor.configKey
import com.twilio.verify.domain.factor.credentialSidKey
import com.twilio.verify.domain.factor.dateCreatedKey
import com.twilio.verify.domain.factor.friendlyNameKey
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.domain.factor.sidKey
import com.twilio.verify.domain.factor.statusKey
import com.twilio.verify.logger.LogLevel
import com.twilio.verify.models.ChallengeListPayload
import com.twilio.verify.models.ChallengeStatus
import com.twilio.verify.models.ChallengeStatus.Approved
import com.twilio.verify.models.ChallengeStatus.Pending
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorStatus.Unverified
import com.twilio.verify.models.FactorStatus.Verified
import com.twilio.verify.models.PushFactorPayload
import com.twilio.verify.models.UpdatePushChallengePayload
import com.twilio.verify.models.UpdatePushFactorPayload
import com.twilio.verify.models.VerifyPushFactorPayload
import com.twilio.verify.networking.NetworkProvider
import com.twilio.verify.networking.Response
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

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [TestKeystore::class])
class TwilioVerifyTest {

  private lateinit var factor: Factor
  private val networkProvider: NetworkProvider = mock()
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
    preferences =
      context.getSharedPreferences("${context.packageName}.$VERIFY_SUFFIX", Context.MODE_PRIVATE)
    twilioVerify =
      TwilioVerify.Builder(context)
        .networkProvider(networkProvider)
        .logLevel(LogLevel.off)
        .loggingService(mock())
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
      .put(dateCreatedKey, toRFC3339Date(Date()))
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(Response(jsonObject.toString(), emptyMap()))
      }
    }
    val accessToken =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorPayload =
      PushFactorPayload("friendly name", factorServiceSid, factorIdentity, "pushToken", accessToken)
    idlingResource.startOperation()
    twilioVerify.createFactor(
      factorPayload,
      { factor ->
        assertEquals(jsonObject.getString(sidKey), factor.sid)
        assertTrue(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
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
  fun `Update a factor should call success`() {
    val sid = "sid"
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
      .put(sidKey, sid)
      .put(friendlyNameKey, "factor name")
      .put(accountSidKey, "accountSid123")
      .put(statusKey, Verified.value)
      .put(configKey, JSONObject().put(credentialSidKey, "credentialSid"))
      .put(dateCreatedKey, toRFC3339Date(Date()))
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(Response(jsonObject.toString(), emptyMap()))
      }
    }
    val updatePushFactorPayload = UpdatePushFactorPayload(sid, "pushToken")
    idlingResource.startOperation()
    twilioVerify.updateFactor(
      updatePushFactorPayload,
      { factor ->
        assertEquals(jsonObject.getString(sidKey), factor.sid)
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
  fun `Verify a factor should call success`() {
    val sid = "sid"
    val verifyFactorPayload = VerifyPushFactorPayload(sid)
    createFactor(sid, Unverified)
    val jsonObject = JSONObject()
      .put(sidKey, sid)
      .put(statusKey, Verified.value)
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(Response(jsonObject.toString(), emptyMap()))
      }
    }
    idlingResource.startOperation()
    twilioVerify.verifyFactor(
      verifyFactorPayload,
      { factor ->
        assertEquals(jsonObject.getString(sidKey), factor.sid)
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
      put(com.twilio.verify.domain.challenge.statusKey, status.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONObject().apply {
              put(labelKey, "label123")
              put(valueKey, "value123")
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
      ).toString()
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        firstValue.invoke(Response(jsonObject.toString(), emptyMap()))
      }
    }
    idlingResource.startOperation()
    twilioVerify.getChallenge(
      challengeSid, factorSid,
      { challenge ->
        assertEquals(challengeSid, challenge.sid)
        assertEquals(status.value, challenge.status.value)
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
  fun `Update challenge should call success`() {
    val factorSid = "factorSid"
    createFactor(factorSid, Verified)
    val challengeSid = "challengeSid"
    val status = Approved
    val updateChallengePayload = UpdatePushChallengePayload(factorSid, challengeSid, status)
    fun challengeResponse(status: ChallengeStatus): JSONObject = JSONObject().apply {
      put(com.twilio.verify.domain.challenge.sidKey, challengeSid)
      put(factorSidKey, factorSid)
      put(createdDateKey, "2020-02-19T16:39:57-08:00")
      put(updatedDateKey, "2020-02-21T18:39:57-08:00")
      put(com.twilio.verify.domain.challenge.statusKey, status.value)
      put(
        detailsKey,
        JSONObject().apply {
          put(messageKey, "message123")
          put(
            fieldsKey,
            JSONArray().apply {
              put(
                0,
                JSONObject().apply {
                  put(labelKey, "label123")
                  put(valueKey, "value123")
                }
              )
            }
          )
          put(dateKey, "2020-02-19T16:39:57-08:00")
        }
          .toString()
      )
      put(
        hiddenDetailsKey,
        JSONObject().apply {
          put("key1", "value1")
        }
          .toString()
      )
      put(expirationDateKey, "2020-02-27T08:50:57-08:00")
    }

    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        when (allValues.size) {
          1 -> lastValue.invoke(
            Response(
              challengeResponse(Pending).toString(),
              mapOf(
                signatureFieldsHeader to listOf(
                  challengeResponse(Pending).keys()
                    .asSequence()
                    .toList()
                    .joinToString(signatureFieldsHeaderSeparator)
                )
              )
            )
          )
          2 -> lastValue.invoke(Response("", emptyMap()))
          3 -> lastValue.invoke(Response(challengeResponse(status).toString(), emptyMap()))
          else -> fail()
        }
      }
    }

    idlingResource.startOperation()
    twilioVerify.updateChallenge(
      updateChallengePayload,
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
  fun `Get all factors should call success`() {
    val factors = mutableListOf<Factor>()
    repeat(3) {
      createFactor("factor$it", Verified)
      factors.add(factor)
    }
    idlingResource.startOperation()
    twilioVerify.getAllFactors(
      {
        assertEquals(factors.size, it.size)
        for (factor in factors) {
          assertNotNull(it.firstOrNull { it.sid == factor.sid })
        }
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
  fun `Get all challenges should call success`() {
    val factorSid = "factorSid123"
    createFactor(factorSid, Verified)
    val challengeListPayload = ChallengeListPayload(factorSid, 1, null, null)
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
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(Response(jsonObject.toString(), emptyMap()))
      }
    }
    idlingResource.startOperation()
    twilioVerify.getAllChallenges(
      challengeListPayload,
      { list ->
        val firstChallenge = list.challenges.first()
        val secondChallenge = list.challenges.last()

        assertEquals(expectedChallenges.length(), list.challenges.size)
        assertEquals(factorSid, firstChallenge.factorSid)
        assertEquals(
          expectedChallenges.getJSONObject(0)
            .getString(sidKey),
          firstChallenge.sid
        )
        assertEquals(factorSid, secondChallenge.factorSid)
        assertEquals(
          expectedChallenges.getJSONObject(1)
            .getString(sidKey),
          secondChallenge.sid
        )

        assertEquals(previousPageToken, list.metadata.previousPageToken)
        assertEquals(nextPageToken, list.metadata.nextPageToken)
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
  fun `Delete factor should call success`() {
    val factorSid = "factorSid123"
    createFactor(factorSid, Verified)
    assertTrue(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
    assertTrue(preferences.contains(factorSid))
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(Response("", emptyMap()))
      }
    }
    idlingResource.startOperation()
    twilioVerify.deleteFactor(
      factorSid,
      {
        assertFalse(preferences.contains(factorSid))
        assertFalse(keys.containsKey((factor as? PushFactor)?.keyPairAlias))
        idlingResource.operationFinished()
      },
      { exception ->
        fail(exception.message)
        idlingResource.operationFinished()
      }
    )
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
      .put(dateCreatedKey, toRFC3339Date(Date()))
    argumentCaptor<(Response) -> Unit>().apply {
      whenever(networkProvider.execute(any(), capture(), any())).then {
        lastValue.invoke(Response(jsonObject.toString(), emptyMap()))
      }
    }
    val accessToken =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val factorPayload =
      PushFactorPayload("friendly name", factorServiceSid, factorIdentity, "pushToken", accessToken)
    idlingResource.startOperation()
    twilioVerify.createFactor(
      factorPayload,
      { factor ->
        this.factor = factor
        assertEquals(factorSid, factor.sid)
        assertEquals(status, factor.status)
        idlingResource.operationFinished()
      },
      { exception ->
        fail(exception.message)
        idlingResource.operationFinished()
      }
    )
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
    put(
      detailsKey,
      JSONObject().apply {
        put(messageKey, "message123")
        put(
          fieldsKey,
          JSONArray().apply {
            put(
              0,
              JSONObject().apply {
                put(labelKey, "label123")
                put(valueKey, "value123")
              }
            )
          }
        )
        put(dateKey, "2020-02-19T16:39:57-08:00")
      }
        .toString()
    )
    put(
      hiddenDetailsKey,
      JSONObject().apply {
        put("key1", "value1")
      }
        .toString()
    )
    put(expirationDateKey, "2020-02-27T08:50:57-08:00")
  }
}

private const val previousPageToken = "previousPageToken"
private const val nextPageToken = "nextPageToken"

private fun metaJSONObject(): JSONObject {
  return JSONObject().apply {
    put(pageKey, 0)
    put(pageSizeKey, 10)
    put(previousPageKey, "https://www.twilio.com?$pageTokenKey=$previousPageToken")
    put(nextPageKey, "https://www.twilio.com?$pageTokenKey=$nextPageToken")
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
    val derSignature = "MEQCIFtun9Ioo-W-juCG7sOl8PPPuozb8cspsUtpu2TxnzP_AiAi1VpFNTr2eK-VX3b1DLHy8" +
      "rPm3MOpTvUH14hyNr0Gfg"
    whenever(mock.sign(any())).thenReturn(Base64.decode(derSignature, FLAGS))
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
