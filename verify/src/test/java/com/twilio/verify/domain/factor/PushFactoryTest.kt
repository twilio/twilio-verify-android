/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.domain.factor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify.data.KeyStorage
import com.twilio.verify.domain.factor.models.PushFactor
import com.twilio.verify.models.FactorType.Push
import com.twilio.verify.networking.Authorization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
  private val authorization: Authorization = mock()
  private val pushFactory =
    PushFactory(
        ApplicationProvider.getApplicationContext<Context>(), authorization, factorProvider,
        keyStorage
    )

  @Test
  fun testCreate_withJWT_shouldReturnFactor() {
    val serviceSid = "ISbb7823aa5dcce90443f856406abd7000"
    val entityId = "1"
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
    pushFactory.create(jwt, friendlyName, pushToken) {
      assertEquals(serviceSid, it?.serviceSid)
      assertEquals(entityId, it?.entityId)
      assertEquals(friendlyName, it?.friendlyName)
      assertEquals(Push, it?.type)
      verify(factorProvider).create(check { pushFactor ->
        assertEquals(binding, pushFactor.binding)
      }, any())
      verify(factorProvider).update(check {
        val factor = it as? PushFactor
        assertNotNull(factor)
        assertEquals(alias, factor?.keyPairAlias)
      })
    }
  }

  @Test
  fun testCreate_noPayloadJwt_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_invalidJWT_shouldNotReturnFactor() {
    val jwt = "test.test"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_noGrant_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7fSwiaXNzIjoiZWI4Mj" +
        "EyZGZjOTUzMzliMmNmYjIyNThjM2YyNGI2ZmIiLCJuYmYiOjE1NzU2MDM3MTgsImV4cCI6MTU3NTYwNzMxOCwic3" +
        "ViIjoiQUM2Y2NiMmNkY2Q4MDM2M2EyNTkyNjZlNzc2YWYwMDAwMCJ9.LTux1Qu0vyjVwVTYVFQwr2J69LB0G3IXK" +
        "SJn0FC-S2I"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_noServiceSid_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7ImVudG" +
        "l0eV9pZCI6IjEiLCJmYWN0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0Yj" +
        "ZmYiIsIm5iZiI6MTU3NTYwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OT" +
        "I2NmU3NzZhZjAwMDAwIn0.yZxVAvTZwdJP1jLyj4hMFYObd74fKXpEgddDt5B_-1w"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_noEntityId_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImZhY3RvciI6InB1c2gifX0sIm" +
        "lzcyI6ImViODIxMmRmYzk1MzM5YjJjZmIyMjU4YzNmMjRiNmZiIiwibmJmIjoxNTc1NjAzNzE4LCJleHAiOjE1Nz" +
        "U2MDczMTgsInN1YiI6IkFDNmNjYjJjZGNkODAzNjNhMjU5MjY2ZTc3NmFmMDAwMDAifQ.0GCjIaEQEYKol5VO14G" +
        "SiDT4l_Sv-J8Z7R2HRIu82o8"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_noFactorType_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEifX0sIm" +
        "lzcyI6ImViODIxMmRmYzk1MzM5YjJjZmIyMjU4YzNmMjRiNmZiIiwibmJmIjoxNTc1NjAzNzE4LCJleHAiOjE1Nz" +
        "U2MDczMTgsInN1YiI6IkFDNmNjYjJjZGNkODAzNjNhMjU5MjY2ZTc3NmFmMDAwMDAifQ.b18nPARv5hUcd72dRLM" +
        "d1m_Nwd6AosfEmRiKoPAJlEQ"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_noPushFactor_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJvdHAifX0sImlzcyI6ImViODIxMmRmYzk1MzM5YjJjZmIyMjU4YzNmMjRiNmZiIiwibmJmIjoxNTc1Nj" +
        "AzNzE4LCJleHAiOjE1NzU2MDczMTgsInN1YiI6IkFDNmNjYjJjZGNkODAzNjNhMjU5MjY2ZTc3NmFmMDAwMDAifQ" +
        ".IgV1ZeL81-nIR9kPovM6IqZCA-bNHizTVgSTkV6AXew"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }

  @Test
  fun testCreate_publicKeyNotCreated_shouldNotReturnFactor() {
    val jwt = "eyJjdHkiOiJ0d2lsaW8tZnBhO3Y9MSIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJqdGkiOiJlYj" +
        "gyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYi0xNTc1NjAzNzE4IiwiZ3JhbnRzIjp7ImF1dGh5Ijp7InNlcn" +
        "ZpY2Vfc2lkIjoiSVNiYjc4MjNhYTVkY2NlOTA0NDNmODU2NDA2YWJkNzAwMCIsImVudGl0eV9pZCI6IjEiLCJmYW" +
        "N0b3IiOiJwdXNoIn19LCJpc3MiOiJlYjgyMTJkZmM5NTMzOWIyY2ZiMjI1OGMzZjI0YjZmYiIsIm5iZiI6MTU3NT" +
        "YwMzcxOCwiZXhwIjoxNTc1NjA3MzE4LCJzdWIiOiJBQzZjY2IyY2RjZDgwMzYzYTI1OTI2NmU3NzZhZjAwMDAwIn" +
        "0.QWrQhpdrJTtXXFwDX9LL4wCy43SWhjS-w5p9C6bcsTk"
    val friendlyName = "factor name"
    val pushToken = "pushToken123"
    whenever(keyStorage.create(any())).thenReturn(null)
    pushFactory.create(jwt, friendlyName, pushToken) {
      fail()
    }
  }
}