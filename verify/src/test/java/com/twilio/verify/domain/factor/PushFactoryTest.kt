/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.IdlingResource
import com.twilio.verify.TwilioVerifyException
import com.twilio.verify.TwilioVerifyException.ErrorCode.InputError
import com.twilio.verify.TwilioVerifyException.ErrorCode.KeyStorageError
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.data.StorageException
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.Factor
import com.twilio.verify.models.FactorStatus
import com.twilio.verify.models.FactorType.Push
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PushFactoryTest {

  private val factorProvider: FactorProvider = mock()
  private val keyStorage: KeyStorage = mock()
  private val pushFactory = PushFactory(factorProvider, keyStorage)
  private val idlingResource = IdlingResource()

  @Test
  fun `Create factor with valid JWT should call success lambda`() {
    val serviceSid = "ISb3a64ae0d2262a2bad5e9870c448b83a"
    val entityId = "YEbd15653d11489b27c1b6255230301815"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    val publicKey = "publicKey123"
    var alias: String? = null
    argumentCaptor<String>().apply {
      whenever(keyStorage.create(capture())).then {
        alias = firstValue
        return@then publicKey
      }
    }
    val pushFactor = PushFactor("1", friendlyName, "1", serviceSid, entityId)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.create(any(), capture(), any())).then {
        firstValue.invoke(pushFactor)
      }
    }
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      verify(factorProvider).create(check { pushFactor ->
        assertEquals(publicKey, pushFactor.publicKey)
        assertEquals(pushToken, pushFactor.pushToken)
        assertEquals(serviceSid, pushFactor.serviceSid)
        assertEquals(entityId, pushFactor.entity)
        assertEquals(friendlyName, pushFactor.friendlyName)
      }, any(), any())
      verify(factorProvider).update(check {
        val factor = it as? PushFactor
        assertNotNull(factor)
        assertEquals(alias, factor?.keyPairAlias)
      })
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with no payload in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with invalid JWT should call error lambda`() {
    val jwt = "test.test"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with no authy api grant in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9fSwianRpIjoiU0swMDEwY2Q3OWM5ODczNWUwY2Q5YmI0OTYwZW" +
        "Y2MmZiOC0xNTgzODUxMjY0Iiwic3ViIjoiQUNjODU2M2RhZjg4ZWQyNmYyMjc2MzhmNTczODcyNmZiZCJ9.SMgmA" +
        "E6N8j8UafDmiB-3x5uK-RZo1u94miScDt_Ld1g"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with no verify grant in JWT should call error lambda`() {
    val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSz" +
        "AwZDViMWU4ZDE5OWUwNjcxN2E2MGMzMjIzOWRhYjdjLTE1ODQwMzQ4MzIiLCJncmFudHMiOnsiYXBpIjp7ImF1dG" +
        "h5X3YxIjpbeyJhY3QiOlsicmVhZCIsImNyZWF0ZSJdLCJyZXMiOiIvU2VydmljZXMvSVMwZmM3MDRiZDczNjZhZD" +
        "BlYWNmMmYzZDVkMTdiYWU3YS9FbnRpdGllcy85Zjg2ZDA4MTg4NGM3ZDY1OWEyZmVhYTBjNTVhZDAxNWEzYmY0Zj" +
        "FiMmIwYjgyMmNkMTVkNmMxNWIwZjAwYTA4L0ZhY3RvcnMifV19fSwiaWF0IjoxNTg0MDM0ODMyLCJleHAiOjE1OD" +
        "QwMzg0MzIsImlzcyI6IlNLMDBkNWIxZThkMTk5ZTA2NzE3YTYwYzMyMjM5ZGFiN2MiLCJzdWIiOiJBQzUxM2FmMD" +
        "NmMzIyOGYxZTg1NThjZWJiYTAxZGMwYjNlIn0.-dDi8zZZ1qSfUdudW5-_iJBMRjNCpVHR6H8hKfWx3zw"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with no service sid in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzLy9FbnRpdGllcy9ZRWJkMTU2NTNkMTE0ODliMjdjMWI2MjU1MjMwMzAxODE1L0ZhY3" +
        "RvcnMifV19fSwianRpIjoiU0swMDEwY2Q3OWM5ODczNWUwY2Q5YmI0OTYwZWY2MmZiOC0xNTgzODUxMjY0Iiwic3" +
        "ViIjoiQUNjODU2M2RhZjg4ZWQyNmYyMjc2MzhmNTczODcyNmZiZCJ9.nP0GmHHkr79_iPbJEOfuRFscKbGZkSUTR" +
        "B-JfnoWBCU"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with no entity id in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImZhY3RvciI6InB1c2giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOl" +
        "t7ImFjdCI6WyJjcmVhdGUiXSwicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OG" +
        "I4M2EvRW50aXRpZXMvWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aS" +
        "I6IlNLMDAxMGNkNzljOTg3MzVlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYW" +
        "Y4OGVkMjZmMjI3NjM4ZjU3Mzg3MjZmYmQifQ.8rJqjYEivNzi3vN8lpAE0FCiuab53IPV7jEzgvA8xOs"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with no factor type in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsInJlcXVpcmUtYmlvbW" +
        "V0cmljcyI6dHJ1ZX0sImFwaSI6eyJhdXRoeV92MSI6W3siYWN0IjpbImNyZWF0ZSJdLCJyZXMiOiIvU2VydmljZX" +
        "MvSVNiM2E2NGFlMGQyMjYyYTJiYWQ1ZTk4NzBjNDQ4YjgzYS9FbnRpdGllcy9ZRWJkMTU2NTNkMTE0ODliMjdjMW" +
        "I2MjU1MjMwMzAxODE1L0ZhY3RvcnMifV19fSwianRpIjoiU0swMDEwY2Q3OWM5ODczNWUwY2Q5YmI0OTYwZWY2Mm" +
        "ZiOC0xNTgzODUxMjY0Iiwic3ViIjoiQUNjODU2M2RhZjg4ZWQyNmYyMjc2MzhmNTczODcyNmZiZCJ9.iCz2ewvcV" +
        "ONSteudnrED4itelvCG5DSU4gW1pCeeHTA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Create factor with not supported factor type in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InRlc3" +
        "QiLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.0zBAhOoidU15E3uT52JAN3tXkEPWNnxhJwQu0gcSsVw"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Keypair not created creating a factor should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    given(keyStorage.create(any())).willAnswer {
      throw TwilioVerifyException(IllegalStateException(), KeyStorageError)
    }
    idlingResource.startOperation()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      assertEquals(KeyStorageError.message, exception.message)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Error in factor provider creating the factor should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
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
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      verify(keyStorage).delete(alias)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Empty keypair in push factor creating a factor should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJpc3MiOiJTSz" +
        "AwMTBjZDc5Yzk4NzM1ZTBjZDliYjQ5NjBlZjYyZmI4IiwiZXhwIjoxNTgzOTM3NjY0LCJncmFudHMiOnsidmVyaW" +
        "Z5Ijp7ImlkZW50aXR5IjoiWUViZDE1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNSIsImZhY3RvciI6InB1c2" +
        "giLCJyZXF1aXJlLWJpb21ldHJpY3MiOnRydWV9LCJhcGkiOnsiYXV0aHlfdjEiOlt7ImFjdCI6WyJjcmVhdGUiXS" +
        "wicmVzIjoiL1NlcnZpY2VzL0lTYjNhNjRhZTBkMjI2MmEyYmFkNWU5ODcwYzQ0OGI4M2EvRW50aXRpZXMvWUViZD" +
        "E1NjUzZDExNDg5YjI3YzFiNjI1NTIzMDMwMTgxNS9GYWN0b3JzIn1dfX0sImp0aSI6IlNLMDAxMGNkNzljOTg3Mz" +
        "VlMGNkOWJiNDk2MGVmNjJmYjgtMTU4Mzg1MTI2NCIsInN1YiI6IkFDYzg1NjNkYWY4OGVkMjZmMjI3NjM4ZjU3Mz" +
        "g3MjZmYmQifQ.R01YC9mfCzIf9W81GUUCMjTwnhzIIqxV-tcdJYuy6kA"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
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
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      verify(keyStorage).delete(alias)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with stored factor should call success`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entityId = "entityId"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = "keyPairAlias"
    val payload = "payload"
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entityId, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    whenever(keyStorage.sign(eq(keyPairAlias), eq(verificationCode))).thenReturn(payload)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.verify(eq(factor), eq(payload), capture(), any())).then {
        firstValue.invoke(factor)
      }
    }
    idlingResource.startOperation()
    pushFactory.verify(sid, verificationCode, {
      assertEquals(serviceSid, it.serviceSid)
      assertEquals(friendlyName, it.friendlyName)
      assertEquals(Push, it.type)
      assertEquals(status, it.status)
      assertEquals(accountSid, it.accountSid)
      assertEquals(entityId, it.entityIdentity)
      assertEquals(sid, it.sid)
      verify(keyStorage).sign(keyPairAlias, verificationCode)
      idlingResource.operationFinished()
    }, {
      fail()
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor without stored factor should call error`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entityId = "entityId"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = "keyPairAlias"
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entityId, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(null)
    idlingResource.startOperation()
    pushFactory.verify(sid, verificationCode, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is StorageException)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with API error should call error`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entityId = "entityId"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = "keyPairAlias"
    val payload = "payload"
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entityId, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    whenever(keyStorage.sign(eq(keyPairAlias), eq(verificationCode))).thenReturn(payload)
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorProvider.verify(eq(factor), eq(payload), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    idlingResource.startOperation()
    pushFactory.verify(sid, verificationCode, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertEquals(expectedException, exception)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }

  @Test
  fun `Verify factor with null alias should call error`() {
    val sid = "sid"
    val verificationCode = "verificationCode"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entityId = "entityId"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = null
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entityId, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    idlingResource.startOperation()
    pushFactory.verify(sid, verificationCode, {
      fail()
      idlingResource.operationFinished()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      idlingResource.operationFinished()
    })
    idlingResource.waitForIdle()
  }
}
