/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.os.Looper.getMainLooper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PushFactoryTest {

  private val factorProvider: FactorProvider = mock()
  private val keyStorage: KeyStorage = mock()
  private val pushFactory = PushFactory(factorProvider, keyStorage)
  private val counter = AtomicInteger(0)

  @Test
  fun `Create factor with valid JWT should call success lambda`() {
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entitySid = "1"
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    val publicKey = "publicKey123"
    val binding = mapOf(pushTokenKey to pushToken, publicKeyKey to publicKey)
    var alias: String? = null
    argumentCaptor<String>().apply {
      whenever(keyStorage.create(capture())).then {
        alias = firstValue
        return@then publicKey
      }
    }
    val pushFactor = PushFactor("1", friendlyName, "1", serviceSid, entitySid)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.create(any(), capture(), any())).then {
        firstValue.invoke(pushFactor)
      }
    }
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      verify(factorProvider).create(check { pushFactor ->
        assertEquals(binding, pushFactor.binding)
        assertEquals(serviceSid, pushFactor.serviceSid)
        assertEquals(entitySid, pushFactor.entitySid)
        assertEquals(friendlyName, pushFactor.friendlyName)
      }, any(), any())
      verify(factorProvider).update(check {
        val factor = it as? PushFactor
        assertNotNull(factor)
        assertEquals(alias, factor?.keyPairAlias)
      })
      counter.decrementAndGet()
    }, {
      fail()
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with no payload in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with invalid JWT should call error lambda`() {
    val jwt = "test.test"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with no grant in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7fSwiaXNzIjoiZWI4Mj" +
        "EyZGZjOTUzMzliMmNmYjIyNThjM2YyNGI2ZmIiLCJuYmYiOjE1NzU2MDM3MTgsImV4cCI6MTU3NTYwNzMxOCwic3" +
        "ViIjoiQUM2Y2NiMmNkY2Q4MDM2M2EyNTkyNjZlNzc2YWYwMDAwMCJ9.LTux1Qu0vyjVwVTYVFQwr2J69LB0G3IXK" +
        "SJn0FC-S2I"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with no service sid in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7ImVudG" +
        "l0eV9pZCI6IjEiLCJmYWN0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0Yj" +
        "ZmYiIsIm5iZiI6MTU3NTYwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OT" +
        "I2NmU3NzZhZjAwMDAwIn0.yZxVAvTZwdJP1jLyj4hMFYObd74fKXpEgddDt5B_-1w"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with no entity sid in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImZhY3RvciI6InB1c2gifX0sIm" +
        "lzcyI6ImViODIxMmRmYzk1MzM5YjJjZmIyMjU4YzNmMjRiNmZiIiwibmJmIjoxNTc1NjAzNzE4LCJleHAiOjE1Nz" +
        "U2MDczMTgsInN1YiI6IkFDNmNjYjJjZGNkODAzNjNhMjU5MjY2ZTc3NmFmMDAwMDAifQ.0GCjIaEQEYKol5VO14G" +
        "SiDT4l_Sv-J8Z7R2HRIu82o8"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with no factor type in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEifX0sIm" +
        "lzcyI6ImViODIxMmRmYzk1MzM5YjJjZmIyMjU4YzNmMjRiNmZiIiwibmJmIjoxNTc1NjAzNzE4LCJleHAiOjE1Nz" +
        "U2MDczMTgsInN1YiI6IkFDNmNjYjJjZGNkODAzNjNhMjU5MjY2ZTc3NmFmMDAwMDAifQ.b18nPARv5hUcd72dRLM" +
        "d1m_Nwd6AosfEmRiKoPAJlEQ"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Create factor with not supported factor type in JWT should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJvdHAifX0sImlzcyI6ImViODIxMmRmYzk1MzM5YjJjZmIyMjU4YzNmMjRiNmZiIiwibmJmIjoxNTc1Nj" +
        "AzNzE4LCJleHAiOjE1NzU2MDczMTgsInN1YiI6IkFDNmNjYjJjZGNkODAzNjNhMjU5MjY2ZTc3NmFmMDAwMDAifQ" +
        ".IgV1ZeL81-nIR9kPovM6IqZCA-bNHizTVgSTkV6AXew"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalArgumentException)
      assertEquals(InputError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Keypair not created creating a factor should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    given(keyStorage.create(any())).willAnswer {
      throw TwilioVerifyException(IllegalStateException(), KeyStorageError)
    }
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      assertEquals(KeyStorageError.message, exception.message)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Error in factor provider creating the factor should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
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
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertEquals(expectedException, exception)
      verify(keyStorage).delete(alias)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Empty keypair in push factor creating a factor should call error lambda`() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
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
    counter.incrementAndGet()
    pushFactory.create(jwt, friendlyName, pushToken, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      verify(keyStorage).delete(alias)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Verify factor with stored factor should call success`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entitySid = "entitySid"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = "keyPairAlias"
    val payload = "payload"
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entitySid, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    whenever(keyStorage.sign(eq(keyPairAlias), eq(factor.sid))).thenReturn(payload)
    argumentCaptor<(Factor) -> Unit>().apply {
      whenever(factorProvider.verify(eq(factor), eq(payload), capture(), any())).then {
        firstValue.invoke(factor)
      }
    }
    counter.incrementAndGet()
    pushFactory.verify(sid, {
      assertEquals(serviceSid, it.serviceSid)
      assertEquals(friendlyName, it.friendlyName)
      assertEquals(Push, it.type)
      assertEquals(status, it.status)
      assertEquals(accountSid, it.accountSid)
      assertEquals(entitySid, it.entitySid)
      assertEquals(sid, it.sid)
      verify(keyStorage).sign(keyPairAlias, factor.sid)
      counter.decrementAndGet()
    }, {
      fail()
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Verify factor without stored factor should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entitySid = "entitySid"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = "keyPairAlias"
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entitySid, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(null)
    counter.incrementAndGet()
    pushFactory.verify(sid, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is StorageException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Verify factor with API error should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entitySid = "entitySid"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = "keyPairAlias"
    val payload = "payload"
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entitySid, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    whenever(keyStorage.sign(eq(keyPairAlias), eq(factor.sid))).thenReturn(payload)
    val expectedException: TwilioVerifyException = mock()
    argumentCaptor<(TwilioVerifyException) -> Unit>().apply {
      whenever(factorProvider.verify(eq(factor), eq(payload), any(), capture())).then {
        firstValue.invoke(expectedException)
      }
    }
    counter.incrementAndGet()
    pushFactory.verify(sid, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertEquals(expectedException, exception)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }

  @Test
  fun `Verify factor with null alias should call error`() {
    val sid = "sid"
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entitySid = "entitySid"
    val friendlyName = "factor name"
    val accountSid = "accountSid"
    val status = FactorStatus.Unverified
    val keyPairAlias = null
    val factor = PushFactor(sid, friendlyName, accountSid, serviceSid, entitySid, status)
    factor.keyPairAlias = keyPairAlias
    whenever(factorProvider.get(sid)).thenReturn(factor)
    counter.incrementAndGet()
    pushFactory.verify(sid, {
      fail()
      counter.decrementAndGet()
    }, { exception ->
      assertTrue(exception.cause is IllegalStateException)
      counter.decrementAndGet()
    })
    counter.waitForEmpty()
  }
}

fun AtomicInteger.waitForEmpty(
  waitFor: Long = 100,
  times: Int = 5
) {
  for (i in 0..times) {
    if (get() > 0) {
      Thread.sleep(waitFor)
      Shadows.shadowOf(getMainLooper())
          .idle()
    } else {
      break
    }
  }
  Shadows.shadowOf(getMainLooper())
      .idle()
  assertTrue(get() == 0)
}