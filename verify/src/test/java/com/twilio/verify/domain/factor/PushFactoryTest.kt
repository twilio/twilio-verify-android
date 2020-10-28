/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.BuildConfig
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.StorageException
import com.twilio.verify.domain.factor.models.Config
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorType.PUSH
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PushFactoryTest {

  private val factorProvider: FactorProvider = mock()
  private val keyStorage: KeyStorage = mock()
  private val context: Context = ApplicationProvider.getApplicationContext()
  private val pushFactory = PushFactory(factorProvider, keyStorage, context)
  private val idlingResource = IdlingResource()

  @Test
  fun `Create factor with valid accessToken should call success lambda`() {
    val serviceSid = "ISb3a64ae0d2262a2bad5e9870c448b83a"
    val accessToken =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    val identity = "factor identity"
    val publicKey = "publicKey123"
    val expectedConfig = mapOf(
      SDK_VERSION_KEY to BuildConfig.VERSION_NAME,
      APP_ID_KEY to "${context.applicationInfo.loadLabel(context.packageManager)}",
      NOTIFICATION_PLATFORM_KEY to FCM_PUSH_TYPE,
      NOTIFICATION_TOKEN_KEY to pushToken
    )
    val expectedBinding = mapOf(PUBLIC_KEY_KEY to publicKey, ALG_KEY to DEFAULT_ALG)
    var alias: String? = null
    argumentCaptor<String>().apply {
      whenever(keyStorage.create(capture())).then {
        alias = firstValue
        return@then publicKey
      }
    }
    val pushFactor =
      PushFactor(
        "1", friendlyName, "1", serviceSid, identity, createdAt = Date(),
        config = Config("credentialSid")
      )
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.create(any(), capture(), any())).then {
        firstValue.invoke(pushFactor)
      }
    }
    idlingResource.startOperation()
    pushFactory.create(
      accessToken, friendlyName, pushToken, serviceSid, identity,
      {
        verify(factorProvider).create(
          check { pushFactor ->
            assertEquals(expectedBinding, pushFactor.binding)
            assertEquals(expectedConfig, pushFactor.config)
            assertEquals(serviceSid, pushFactor.serviceSid)
            assertEquals(identity, pushFactor.identity)
            assertEquals(friendlyName, pushFactor.friendlyName)
          },
          any(), any()
        )
        verify(factorProvider).save(
          check {
            val factor = it as? PushFactor
            assertNotNull(factor)
            assertEquals(alias, factor?.keyPairAlias)
          }
        )
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
  fun `Keypair not created creating a factor should call error lambda`() {
    val accessToken =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    val identity = "factor identity"
    val serviceSid = "factor serviceSid"
    given(keyStorage.create(any())).willAnswer {
      throw TwilioVerifyException(IllegalStateException(), KeyStorageError)
    }
    idlingResource.startOperation()
    pushFactory.create(
      accessToken, friendlyName, pushToken, serviceSid, identity,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        assertEquals(KeyStorageError.message, exception.message)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error in factor provider creating the factor should call error lambda`() {
    val accessToken =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    val identity = "factor identity"
    val serviceSid = "factor serviceSid"
    val publicKey = "publicKey123"
    var alias = ""
    argumentCaptor<String>().apply {
      whenever(keyStorage.create(capture())).then {
        alias = firstValue
        publicKey
      }
    }
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorProvider.create(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    pushFactory.create(
      accessToken, friendlyName, pushToken, serviceSid, identity,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertEquals(expectedException, exception)
        verify(keyStorage).delete(alias)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Empty keypair in push factor creating a factor should call error lambda`() {
    val accessToken =
      "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    val identity = "factor identity"
    val serviceSid = "factor serviceSid"
    val publicKey = "publicKey123"
    var alias = ""
    argumentCaptor<String>().apply {
      whenever(keyStorage.create(capture())).then {
        alias = firstValue
        publicKey
      }
    }
    val pushFactor: PushFactor = mock()
    whenever(pushFactor.keyPairAlias).thenReturn(null)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.create(any(), capture(), any())).then {
        firstValue.invoke(pushFactor)
      }
    }
    idlingResource.startOperation()
    pushFactory.create(
      accessToken, friendlyName, pushToken, serviceSid, identity,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is IllegalStateException)
        verify(keyStorage).delete(alias)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with stored factor should call success`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val credentialSid = "credentialSid"
    val keyPairAlias = "keyPairAlias"
    val payload = "payload"
    val factor =
      PushFactor(
        sid,
        friendlyName,
        accountSid,
        serviceSid,
        identity,
        status,
        Date(),
        Config("credentialSid")
      )
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    whenever(keyStorage.signAndEncode(eq(keyPairAlias), eq(sid))).thenReturn(payload)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.verify(eq(factor), eq(payload), capture(), any())).then {
        firstValue.invoke(factor)
      }
    }
    idlingResource.startOperation()
    pushFactory.verify(
      sid,
      {
        assertEquals(serviceSid, it.serviceSid)
        assertEquals(friendlyName, it.friendlyName)
        assertEquals(PUSH, it.type)
        assertEquals(status, it.status)
        assertEquals(accountSid, it.accountSid)
        assertEquals(identity, it.identity)
        assertEquals(sid, it.sid)
        assertEquals(credentialSid, (it as PushFactor).config.credentialSid)
        verify(keyStorage).signAndEncode(keyPairAlias, it.sid)
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
  fun `Verify factor without stored factor should call error`() {
    val sid = "sid"
    whenever(factorProvider.get(sid)).thenReturn(null)
    idlingResource.startOperation()
    pushFactory.verify(
      sid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        assertTrue(exception.cause is StorageException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with API error should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val credentialSid = "credentialSid"
    val keyPairAlias = "keyPairAlias"
    val payload = "payload"
    val factor =
      PushFactor(
        sid,
        friendlyName,
        accountSid,
        serviceSid,
        identity,
        status,
        Date(),
        Config("credentialSid")
      )
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    whenever(keyStorage.signAndEncode(eq(keyPairAlias), eq(sid))).thenReturn(payload)
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorProvider.verify(eq(factor), eq(payload), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    pushFactory.verify(
      sid,
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
  fun `Verify factor with null alias should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val credentialSid = "credentialSid"
    val keyPairAlias = null
    val factor =
      PushFactor(
        sid, friendlyName, accountSid, serviceSid, identity, status, Date(), Config(credentialSid)
      )
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    idlingResource.startOperation()
    pushFactory.verify(
      sid,
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
  fun `Update factor with stored factor should call success`() {
    val sid = "sid"
    val pushToken = "pushToken"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val credentialSid = "credentialSid"
    val status = FactorStatus.Unverified
    val factor =
      PushFactor(
        sid,
        friendlyName,
        accountSid,
        serviceSid,
        identity,
        status,
        Date(),
        Config("credentialSid")
      )
    whenever(factorProvider.get(sid)).thenReturn(factor)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.update(any(), capture(), any())).then {
        firstValue.invoke(factor)
      }
    }
    idlingResource.startOperation()
    pushFactory.update(
      sid, pushToken,
      {
        verify(factorProvider).update(
          check { updateFactorPayload ->
            assertEquals(sid, updateFactorPayload.factorSid)
            assertEquals(serviceSid, updateFactorPayload.serviceSid)
            assertEquals(friendlyName, updateFactorPayload.friendlyName)
            assertEquals(identity, updateFactorPayload.identity)
            assertEquals(PUSH, updateFactorPayload.type)
            assertEquals(pushToken, updateFactorPayload.config[NOTIFICATION_TOKEN_KEY])
          },
          any(), any()
        )
        assertEquals(serviceSid, it.serviceSid)
        assertEquals(friendlyName, it.friendlyName)
        assertEquals(PUSH, it.type)
        assertEquals(status, it.status)
        assertEquals(accountSid, it.accountSid)
        assertEquals(identity, it.identity)
        assertEquals(sid, it.sid)
        assertEquals(credentialSid, (it as PushFactor).config.credentialSid)
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
  fun `Update factor without stored factor should call error`() {
    val sid = "sid"
    whenever(factorProvider.get(sid)).thenReturn(null)
    idlingResource.startOperation()
    pushFactory.update(
      sid, "pushToken",
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        verify(factorProvider, never()).update(any(), any(), any())
        assertTrue(exception.cause is StorageException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Update factor with API error should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val factor =
      PushFactor(
        sid,
        friendlyName,
        accountSid,
        serviceSid,
        identity,
        status,
        Date(),
        Config("credentialSid")
      )
    whenever(factorProvider.get(sid)).thenReturn(factor)
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorProvider.update(any(), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    pushFactory.update(
      sid, "pushToken",
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
  fun `Delete factor with stored factor should call success and delete factor alias from key storage`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val credentialSid = "credentialSid"
    val alias = "keyPairAlias"
    val factor =
      PushFactor(
        sid,
        friendlyName,
        accountSid,
        serviceSid,
        identity,
        status,
        Date(),
        Config(credentialSid)
      ).apply {
        keyPairAlias = alias
      }
    whenever(factorProvider.get(sid)).thenReturn(factor)
    argumentCaptor<() -> Unit>().apply {
      whenever(factorProvider.delete(eq(factor), capture(), any())).then {
        firstValue.invoke()
      }
    }
    idlingResource.startOperation()
    pushFactory.delete(
      sid,
      {
        verify(keyStorage).delete(alias)
        verify(factorProvider).delete(
          check { factor ->
            assertEquals(sid, factor.sid)
            assertEquals(serviceSid, factor.serviceSid)
            assertEquals(friendlyName, factor.friendlyName)
            assertEquals(identity, factor.identity)
            assertEquals(PUSH, factor.type)
          },
          any(), any()
        )
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
  fun `Delete factor without stored factor should call error`() {
    val sid = "sid"
    whenever(factorProvider.get(sid)).thenReturn(null)
    idlingResource.startOperation()
    pushFactory.delete(
      sid,
      {
        fail()
        idlingResource.operationFinished()
      },
      { exception ->
        verify(factorProvider, never()).update(any(), any(), any())
        assertTrue(exception.cause is StorageException)
        idlingResource.operationFinished()
      }
    )
    idlingResource.waitForIdle()
  }

  @Test
  fun `Delete factor with API error should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val identity = "identity"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val credentialSid = "credentialSid"
    val factor =
      PushFactor(
        sid, friendlyName, accountSid, serviceSid, identity, status, Date(), Config(credentialSid)
      )
    whenever(factorProvider.get(sid)).thenReturn(factor)
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorProvider.delete(eq(factor), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    pushFactory.delete(
      sid,
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
  fun `Delete all factors should delete factors and call then`() {
    val factor1 = PushFactor(
      "sid1",
      "friendlyName",
      "accountSid",
      "serviceSid",
      "identity",
      FactorStatus.Verified,
      Date(),
      Config("credentialSid")
    ).apply { keyPairAlias = "alias1" }
    val factor2 = PushFactor(
      "sid2",
      "friendlyName",
      "accountSid",
      "serviceSid",
      "identity",
      FactorStatus.Verified,
      Date(),
      Config("credentialSid")
    ).apply { keyPairAlias = "alias2" }
    whenever(factorProvider.getAll()).thenReturn(listOf(factor1, factor2))
    pushFactory.deleteAllFactors {
      verify(factorProvider).delete(factor1)
      verify(factorProvider).delete(factor2)
      verify(keyStorage).delete(factor1.keyPairAlias!!)
      verify(keyStorage).delete(factor2.keyPairAlias!!)
    }
  }
}
